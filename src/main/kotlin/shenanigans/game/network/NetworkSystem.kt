package shenanigans.game.network

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.util.Transform
import shenanigans.game.network.client.Client
import kotlin.reflect.KClass

class Synchronized : Component {
    var serverId : Int? = null
}

class NetworkSystem : System{

    private val clientIds : BiMap<EntityId, EntityId> = HashBiMap.create()

    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Synchronized::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        resources.get<EventQueue>().iterate<EntityRegistrationPacket>().forEach { packet ->
            if(packet.clientId == Client.getId()) {
                clientIds[packet.clientEntityId] = packet.serverEntityId!!
                println("WHaHOOO")
                return
            }

            val newId = lifecycle.add(
                packet.components.asSequence()
            )

            clientIds[newId] = packet.serverEntityId!!
        }

        resources.get<EventQueue>().iterate<EntityPacket>().forEach { packet ->
            packet.entities.forEach() {entity ->
                if(!clientIds.containsValue(entity.key)) {
                    return
                }

                var position = entities[clientIds.inverse()[entity.key]!!]!!.component<Transform>().get().position
                position.lerp((entity.value[Transform::class]!! as Transform).position, 1f/7.5f)
            }
        }

        for (entity in entities) {
            if(entity.component<Synchronized>().get().serverId == null) {
                Client.createNetworkedEntity(entity)
                entity.component<Synchronized>().get().serverId = -1
                continue
            }
        }

        Client.updateEntities(EntityPacket(entities.filter {it.component<Synchronized>().get().serverId == -1}, clientIds, -1))
    }
}