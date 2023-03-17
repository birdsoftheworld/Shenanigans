package shenanigans.engine.network

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.net.MessageDelivery
import shenanigans.engine.net.NetworkEventQueue
import shenanigans.engine.net.events.ConnectionEvent
import shenanigans.engine.term.Logger
import shenanigans.engine.util.Transform
import shenanigans.game.network.EntityMovementPacket
import shenanigans.game.network.EntityRegistrationPacket
import kotlin.reflect.KClass

class ServerUpdateSystem : System {
    override fun executeNetwork(
        resources: ResourcesView,
        eventQueues: EventQueues<NetworkEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(emptySet())

        eventQueues.own.receive(EntityMovementPacket::class).forEach { packet ->
            packet.entities.forEach { entity ->
                if (entities[entity.key] != null) {
                    if (entities[entity.key]?.componentOpt<Transform>() == null) {
                        Logger.warn("Server Update", "entity does not have a transform!")
                    }
                    entities[entity.key]?.component<Transform>()!!.get().position = (entity.value).position
                } else {
                    Logger.warn("Entity Update", "entity does not exist: " + entity.key)
                }
            }
        }

        eventQueues.network.queueLater(EntityMovementPacket(entities))
    }
}

class ServerRegistrationSystem : System {
    override fun executeNetwork(
        resources: ResourcesView,
        eventQueues: EventQueues<NetworkEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(emptySet())

        eventQueues.network.receive(EntityRegistrationPacket::class).forEach { entityRegistrationPacket ->
            if (entities[entityRegistrationPacket.id] != null) {
                Logger.warn("Entity Registration", "Duplicate ID: " + entityRegistrationPacket.id)
            }
            lifecycle.add(
                entityRegistrationPacket.entity.values.asSequence(),
                entityRegistrationPacket.id,
            )
            Logger.log("Entity Registration", entityRegistrationPacket.id.toString())
            eventQueues.network.queueLater(entityRegistrationPacket)
        }
    }
}


class FullEntitySyncSystem : System {
    override fun executeNetwork(
        resources: ResourcesView,
        eventQueues: EventQueues<NetworkEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(emptySet())

        eventQueues.own.receive(ConnectionEvent::class).forEach { connectionEvent ->
            entities.forEach {
                eventQueues.network.queueNetwork(
                    EntityRegistrationPacket(it),
                    delivery = MessageDelivery.ReliableOrdered,
                    recipient = connectionEvent.connectionId
                )
            }
        }
    }
}