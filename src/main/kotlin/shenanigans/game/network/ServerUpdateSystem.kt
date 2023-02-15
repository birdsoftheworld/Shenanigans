package shenanigans.engine.network

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.net.events.ConnectionEvent
import shenanigans.engine.util.Transform
import shenanigans.game.network.EntityMovementPacket
import shenanigans.game.network.EntityRegistrationPacket
import shenanigans.game.network.Synchronized
import kotlin.reflect.KClass

class ServerUpdateSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Synchronized::class)
    }

    override fun executeNetwork(
        resources: ResourcesView,
        eventQueues: EventQueues,
        entities: EntitiesView,
        lifecycle: EntitiesLifecycle
    ) {
        eventQueues.network.iterate<EntityMovementPacket>().forEach { packet ->
            packet.entities.forEach() { entity ->
                if(entities[entity.key]?.componentOpt<Transform>() == null) {
                    println("entity does not have a transform!")
                }
                entities[entity.key]?.component<Transform>()!!.get().position = (entity.value).position
            }
        }

        eventQueues.network.queueLater(EntityMovementPacket(entities))
    }
}

class ServerRegistrationSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf()
    }

    override fun executeNetwork(
        resources: ResourcesView,
        eventQueues: EventQueues,
        entities: EntitiesView,
        lifecycle: EntitiesLifecycle
    ) {

        eventQueues.network.iterate<EntityRegistrationPacket>().forEach {entityRegistrationPacket ->
            lifecycle.add(
                entityRegistrationPacket.entity.components.values.map{it.component}.asSequence()
            )
            eventQueues.network.queueLater(entityRegistrationPacket)
        }
    }
}


class FullEntitySyncSystem : System {
    override fun query(): Iterable<KClass<out Component>> = setOf()

    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues,
        entities: EntitiesView,
        lifecycle: EntitiesLifecycle
    ) {

        eventQueues.network.iterate<ConnectionEvent>().forEach { connectionEvent ->
            val connection = connectionEvent.connection
            entities.forEach {
                val packet = EntityRegistrationPacket(it)
            }
        }
    }
}