package shenanigans.game.network

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.net.NetworkEventQueue
import shenanigans.engine.term.Logger
import shenanigans.engine.util.Transform
import kotlin.reflect.KClass

class NetworkSystem : System {

    override fun executeNetwork(
        resources: ResourcesView,
        eventQueues: EventQueues<NetworkEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(setOf(Synchronized::class))

        eventQueues.own.receive(EntityMovementPacket::class).forEach update@{ packet ->
            val entities = query(setOf(Synchronized::class))
            packet.entities.forEach packet@{ entity ->
                if (entities[entity.key] == null) {
                    Logger.warn("Entity Movement", "entity does not exist: " + entity.key)
                    return@packet
                }

                //FIXME
                if (entities[entity.key]!!.componentOpt<Transform>() == null) {
                    return@packet
                }

                val position = entities[entity.key]!!.component<Transform>().get().position
                position.lerp((entity.value).position, 1f / 3f)
            }
        }

        eventQueues.own.receive(EntityRegistrationPacket::class).forEach registration@{ packet ->
            if (entities[packet.id] != null) {
                entities[packet.id]!!.component<Synchronized>().get().registration = RegistrationStatus.Registered
                Logger.log("Network System", "WHaHOOO")
                return@registration
            }

            lifecycle.add(
                packet.entity.values.asSequence(),
                packet.id,
            )

            Logger.log("Network System", "WahoOO!")
        }

        entities.filter { it.component<Synchronized>().get().registration == RegistrationStatus.Disconnected }.forEach {
            eventQueues.network.queueLater(EntityRegistrationPacket(it))
            it.component<Synchronized>().get().registration = RegistrationStatus.Sent
        }

        eventQueues.network.queueLater(EntityMovementPacket(entities.filter {
            it.component<Synchronized>().get().registration == RegistrationStatus.Registered
        }))
    }
}