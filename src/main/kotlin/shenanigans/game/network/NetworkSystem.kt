package shenanigans.game.network

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.net.NetworkEventQueue
import shenanigans.engine.term.Logger
import shenanigans.engine.util.Transform
import shenanigans.game.KeyboardPlayer
import kotlin.reflect.KClass

class NetworkSystem : System {

    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Synchronized::class)
    }

    override fun executeNetwork(
        resources: ResourcesView,
        eventQueues: EventQueues<NetworkEventQueue>,
        entities: EntitiesView,
        lifecycle: EntitiesLifecycle
    ) {
        eventQueues.own.receive(EntityMovementPacket::class).forEach update@{ packet ->
            packet.entities.forEach() packet@{ entity ->
                if (entities[entity.key] == null) {
                    return@packet
                }

                //FIXME
                if (entities[entity.key]!!.componentOpt<KeyboardPlayer>() != null) {
                    return@packet
                }
                val position = entities[entity.key]!!.component<Transform>().get().position
                position.lerp((entity.value).position, 1f / 7.5f)
            }
        }

        eventQueues.own.receive(EntityRegistrationPacket::class).forEach registration@{ packet ->
            if (entities[packet.id] != null) {
                entities[packet.id]!!.component<Synchronized>().get().connected = true
                Logger.log("Network System", "WHaHOOO")
                return@registration
            }

            lifecycle.addWithID(
                packet.entity.values.asSequence(), packet.id
            )

            Logger.log("Network System", "WahoOO!")
        }

        entities.filter { !it.component<Synchronized>().get().connected }.forEach {
            eventQueues.network.queueLater(EntityRegistrationPacket(it))
        }

        eventQueues.network.queueLater(EntityMovementPacket(entities.filter {
            it.component<Synchronized>().get().connected
        }))
    }
}