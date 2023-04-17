package shenanigans.game.network

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.net.NetworkEventQueue
import shenanigans.engine.net.events.ConnectionEvent
import shenanigans.engine.net.events.ConnectionEventType
import kotlin.reflect.KClass

abstract class NetworkUpdateSystem : System {
    override fun executeNetwork(
        resources: ResourcesView,
        eventQueues: EventQueues<NetworkEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(setOf(Synchronized::class))

        eventQueues.network.receive(EntityUpdatePacket::class).forEach {
            updateEntities(it, entities, eventQueues.network)
        }

        eventQueues.network.queueNetwork(getUpdatePacket(synchronizedComponents(), entities, eventQueues.network))
    }

    abstract fun getUpdatePacket(
        components: Iterable<KClass<out Component>>,
        entities: QueryView,
        eventQueue: NetworkEventQueue
    ) : EntityUpdatePacket

    abstract fun updateEntities(
        updatePacket: EntityUpdatePacket,
        entities: QueryView,
        eventQueue: NetworkEventQueue
    )
}

abstract class NetworkConnectionSystem() : System {
    override fun executeNetwork(
        resources: ResourcesView,
        eventQueues: EventQueues<NetworkEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        eventQueues.network.receive(ConnectionEvent::class)
            .filter { it.type == ConnectionEventType.Connect }
            .forEach { connect(it, query(setOf(Synchronized::class)), eventQueues.network, lifecycle) }

        eventQueues.network.receive(ConnectionEvent::class)
            .filter { it.type == ConnectionEventType.Disconnect }
            .forEach { connect(it, query(setOf(Synchronized::class)), eventQueues.network, lifecycle) }
    }

    abstract fun connect(
        event: ConnectionEvent,
        entities: QueryView,
        eventQueue: NetworkEventQueue,
        lifecycle: EntitiesLifecycle,
    )

    abstract fun disconnect(
        event: ConnectionEvent,
        entities: QueryView,
        eventQueue: NetworkEventQueue,
        lifecycle: EntitiesLifecycle,
    )
}

abstract class NetworkRegistrationSystem: System {
    override fun executeNetwork(
        resources: ResourcesView,
        eventQueues: EventQueues<NetworkEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        eventQueues.network.receive(EntityRegistrationPacket::class).forEach {
            register(
                it,
                query(setOf(Synchronized::class)),
                eventQueues.network,
                lifecycle
            )
        }
    }

    abstract fun register(
        registrationPacket: EntityRegistrationPacket,
        entities: QueryView,
        eventQueue: NetworkEventQueue,
        lifecycle: EntitiesLifecycle
    )
}