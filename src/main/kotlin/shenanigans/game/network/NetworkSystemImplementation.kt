package shenanigans.game.network

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.net.NetworkEventQueue
import shenanigans.engine.net.events.ConnectionEvent
import shenanigans.engine.net.events.ConnectionEventType
import java.util.*
import kotlin.reflect.KClass

abstract class NetworkUpdateSystem : System {

    private var lastUpdate : MutableMap<UUID, Map<KClass<out Component>, Int>> = mutableMapOf()

    override fun executeNetwork(
        resources: ResourcesView,
        eventQueues: EventQueues<NetworkEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(setOf(Synchronized::class))

        lastUpdate.keys.filter { entities[it] == null }.forEach{
            lastUpdate.remove(it)
            deleteEntity(it, eventQueues)
        }


        eventQueues.network.receive(EntityUpdatePacket::class).forEach {
            updateEntities(
                it,
                entities,
                eventQueues.network)
        }

        eventQueues.network.queueNetwork(
            getUpdatePacket(
                synchronizedComponents(),
                entities.filter { entity ->
                        lastUpdate.containsKey(entity.id) &&
                        lastUpdate[entity.id]!!.filter { lastVersion ->
                            entity.component(lastVersion.key).version() != lastVersion.value
                        }.isNotEmpty()
                },
                eventQueues.network
            )
        )

        entities.forEach { entity ->
            lastUpdate[entity.id] = synchronizedComponents()
                    .filter { entity.componentOpt(it.component) != null }
                    .associate { it.component to entity.component(it.component).version() }
        }
    }

    abstract fun getUpdatePacket(
        components: Set<SynchronizedComponent>,
        entities: Sequence<EntityView>,
        eventQueue: NetworkEventQueue
    ): EntityUpdatePacket

    abstract fun updateEntities(
        updatePacket: EntityUpdatePacket,
        entities: QueryView,
        eventQueue: NetworkEventQueue
    )

    open fun deleteEntity(entity: UUID, eventQueues: EventQueues<NetworkEventQueue>) {
        eventQueues.network.queueNetwork(EntityDeRegistrationPacket(entity))
    }
}

abstract class NetworkConnectionSystem : System {
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
            .forEach { disconnect(it, query(setOf(Synchronized::class)), eventQueues.network, lifecycle) }
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

abstract class NetworkRegistrationSystem : System {
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

        eventQueues.network.receive(EntityDeRegistrationPacket::class).forEach {
            deregister(
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

    open fun deregister(
        deregistrationPacket: EntityDeRegistrationPacket,
        entities: QueryView,
        eventQueue: NetworkEventQueue,
        lifecycle: EntitiesLifecycle
    ) {
        if(entities[deregistrationPacket.id] != null) {
            lifecycle.del(deregistrationPacket.id)
        }
    }
}