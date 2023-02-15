package shenanigans.game.network

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
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
        eventQueues: EventQueues,
        entities: EntitiesView,
        lifecycle: EntitiesLifecycle
    ) {
        eventQueues.network.iterate<EntityMovementPacket>().forEach update@{ packet ->
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

        eventQueues.network.iterate<EntityRegistrationPacket>().forEach registration@{ packet ->
            if (entities[packet.entity.id] != null) {
                entities[packet.entity.id]!!.component<Synchronized>().get().connected = true
                Logger.log("Network System", "WHaHOOO")
                return@registration
            }

            lifecycle.addWithID(
                packet.entity.components.values.map { it.component }.asSequence(), packet.entity.id
            )

            Logger.log("Network System", "WahoOO!")
        }

        entities.filter { !it.component<Synchronized>().get().connected }.forEach {
            eventQueues.network.queueLater(EntityRegistrationPacket(it, ))
        }

        eventQueues.network.queueLater(EntityMovementPacket(entities.filter {
            it.component<Synchronized>().get().connected
        }))
    }
}