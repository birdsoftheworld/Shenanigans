package shenanigans.game.network.server

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.util.Transform
import shenanigans.game.network.EntityPacket
import shenanigans.game.network.EntityRegistrationPacket
import shenanigans.game.network.Synchronized
import kotlin.reflect.KClass

class EntityUpdateSystem : System{
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Synchronized::class)
    }

    override fun execute(resources: ResourcesView, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val eventQueue = resources.get<EventQueue>()

        eventQueue.iterate<EntityPacket>().forEach {event ->
            entities.forEach {entity ->
                if(entity.id == event.id) {
                    entity.component<Transform>().get().position = (event.components[Transform::class]!! as Transform).position
                }
            }
        }
    }
}


class EntityRegistrationSystem : System{
    override fun query(): Iterable<KClass<out Component>> {
        return setOf()
    }

    override fun execute(resources: ResourcesView, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val eventQueue = resources.get<EventQueue>()

        if(Server.packet) {
            true
//            println("WAHOOO")
//            ServerW.packet = false
        }

        eventQueue.iterate<EntityRegistrationPacket>().forEach {entityRegistrationPacket ->
            entityRegistrationPacket.serverEntityId = lifecycle.add(
                entityRegistrationPacket.components.asSequence()
            )

            println("registered")

            resources.get<Server>().registerEntity(entityRegistrationPacket)
        }
    }

}