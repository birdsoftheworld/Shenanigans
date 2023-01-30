package shenanigans.game.network

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.network.client.Client
import shenanigans.engine.term.Logger
import shenanigans.engine.util.Transform
import shenanigans.game.KeyboardPlayer
import kotlin.reflect.KClass

class Synchronized : Component {
    var serverId : EntityId? = null
}

class NetworkSystem : System{

    private val clientIds : BiMap<EntityId, EntityId> = HashBiMap.create()

    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Synchronized::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {

        resources.get<EventQueue>().iterate<EntityPacket>().forEach update@{ packet ->
            packet.entities.forEach() packet@{entity ->
                if(!clientIds.containsValue(entity.key)) {
                    return@packet
                }

                //FIXME
                if(entities[clientIds.inverse()[entity.key]!!]!!.componentOpt<KeyboardPlayer>() != null) {
                    return@packet
                }
                val position = entities[clientIds.inverse()[entity.key]!!]!!.component<Transform>().get().position
                position.lerp((entity.value[Transform::class]!! as Transform).position, 1f/7.5f)
            }
        }

        resources.get<EventQueue>().iterate<EntityRegistrationPacket>().forEach registration@{ packet ->
            println(packet.serverEntityId)
            if(packet.clientId == Client.getId()) {
                clientIds[packet.clientEntityId] = packet.serverEntityId!!
                entities.get(packet.clientEntityId)!!.component<Synchronized>().get().serverId = packet.serverEntityId
                Logger.log("Network System","WHaHOOO")
                return@registration
            }

            packet.components.forEach() {
                if(it is Synchronized) {
                    it.serverId = EntityId(-1)
                }
                println(it)
            }

            val newId = lifecycle.add(
                packet.components.asSequence()
            )
            Logger.log("Network System", "WahoOO!")

            clientIds[newId] = packet.serverEntityId!!
        }

        for (entity in entities) {
            if(entity.component<Synchronized>().get().serverId == null) {
                Client.createNetworkedEntity(entity)
                entity.component<Synchronized>().get().serverId = EntityId(-1)
                continue
            }
        }

        Client.updateEntities(EntityPacket(entities.filter {it.component<Synchronized>().get().serverId != EntityId(-1) }, clientIds))
    }
}