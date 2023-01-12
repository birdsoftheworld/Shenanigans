package shenanigans.game.network

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.game.network.client.Client
import kotlin.reflect.KClass

class Synchronized : Component

class NetworkSystem : System{

    private val clientIds : HashMap<EntityId, EntityId?> = HashMap()
    private val serverIds : HashMap<EntityId, EntityId> = HashMap()

    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Synchronized::class)
    }

    override fun execute(resources: ResourcesView, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        resources.get<EventQueue>().iterate<EntityRegistrationPacket>().forEach { packet ->
            if(packet.clientId == Client.getId()) {
                clientIds[packet.clientEntityId] = packet.serverEntityId
                serverIds[packet.serverEntityId!!] = packet.clientEntityId
                println("WHaHOOO")
                return
            }

            val newId = lifecycle.add(
                packet.components.asSequence()
            )

            clientIds[newId] = packet.serverEntityId
            serverIds[packet.serverEntityId!!] = newId

            println("REGISTERED")
        }

        for (entity in entities) {
            if(!clientIds.containsKey(entity.id)) {
                Client.createNetworkedEntity(entity)
                clientIds[entity.id] = null
                continue
            }

            if(clientIds[entity.id] == null) {
                continue
            }

            Client.sendEntity(entity, clientIds[entity.id]!!)
        }
    }
}