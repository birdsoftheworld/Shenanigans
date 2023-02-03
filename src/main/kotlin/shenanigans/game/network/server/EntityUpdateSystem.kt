package shenanigans.engine.network.server

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.network.Server
import shenanigans.engine.util.Transform
import shenanigans.game.network.ConnectionEvent
import shenanigans.game.network.EntityUpdatePacket
import shenanigans.game.network.EntityRegistrationPacket
import shenanigans.game.network.Synchronized
import kotlin.reflect.KClass

class EntityUpdateSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Synchronized::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val eventQueue = resources.get<EventQueue>()

        eventQueue.iterate<EntityUpdatePacket>().forEach { packet ->
            packet.entities.forEach() { entity ->
                if(entities[entity.key]?.component<Transform>() == null) {
                    println("entity does not have a transform!")
                }
                entities[entity.key]?.component<Transform>()!!.get().position = (entity.value[Transform::class]!! as Transform).position
            }
        }

//        eventQueue.queueNetworked()
    }
}


class ServerRegistrationSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf()
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val eventQueue = resources.get<EventQueue>()

        eventQueue.iterate<EntityRegistrationPacket>().forEach {entityRegistrationPacket ->
            entityRegistrationPacket.serverEntityId = lifecycle.add(
                entityRegistrationPacket.components.asSequence()
            )

//            eventQueue.queueNetworked()
        }
    }
}


class FullEntitySyncSystem : System {
    override fun query(): Iterable<KClass<out Component>> = setOf()

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val eventQueue = resources.get<EventQueue>()
        val server = resources.get<Server>()

        eventQueue.iterate<ConnectionEvent>().forEach { connectionEvent ->
            val connection = connectionEvent.connection
            entities.forEach {
                val packet = EntityRegistrationPacket(it)
                packet.serverEntityId = it.id
                server.registerEntityTo(connection.id, packet)
            }
        }
    }
}