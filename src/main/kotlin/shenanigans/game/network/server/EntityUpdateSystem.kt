package shenanigans.game.network.server

import shenanigans.engine.ecs.*
import shenanigans.engine.events.Event
import shenanigans.engine.events.EventQueue
import shenanigans.game.network.EntityRegistrationPacket
import shenanigans.game.network.Synchronized
import kotlin.reflect.KClass

class EntityUpdateSystem : System{
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Synchronized::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        TODO("Not yet implemented")
    }
}


class EntityRegistrationSystem : System{
    override fun query(): Iterable<KClass<out Component>> {
        return setOf()
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val events = resources.get<EventQueue>()

        events.events.forEach {
            if (it is EntityRegistrationPacket) {
                lifecycle.add(
                    it.components.asSequence()
                )

            }
        }
    }

}