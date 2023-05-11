package shenanigans.game.network

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.net.NetworkEventQueue
import shenanigans.engine.net.events.ConnectionEvent
import shenanigans.engine.term.Logger
import shenanigans.engine.util.Transform
import kotlin.reflect.KClass

class ClientUpdateSystem : NetworkUpdateSystem() {
    override fun getUpdatePacket(
        components: Set<SynchronizedComponent>,
        entities: Sequence<EntityView>,
        eventQueue: NetworkEventQueue
    ): EntityUpdatePacket {
        return EntityUpdatePacket(
            entities.filter {
                val synchronized = it.component<Synchronized>()

                synchronized.get().registration == RegistrationStatus.Registered &&
                        synchronized.get().ownerEndpoint == eventQueue.getEndpoint()
            }.map { entity ->
                entity.id to entity.entity.components.filter { component ->
                    components.map { it.component }.contains(component.key)
                }.mapValues { it.value.component }
            }.toMap()
        )
    }

    override fun updateEntities(updatePacket: EntityUpdatePacket, entities: QueryView, eventQueue: NetworkEventQueue) {
        updatePacket.entities.forEach packet@{ packetEntity ->
            val entity = entities[packetEntity.key]

            if (entity == null) {
                Logger.warn("Entity Update Packet", "Received packet for unregistered entity with ID " + packetEntity.key)
                return@packet
            }

            if (entity.component<Synchronized>().get().ownerEndpoint == eventQueue.getEndpoint()) {
                return@packet
            }

            synchronizedComponents().forEach {synchronizedComponent ->
                entity.component(synchronizedComponent.component).replace(
                    synchronizedComponent.updateClient(
                        entity.component(synchronizedComponent.component).get(),
                        packetEntity.value[synchronizedComponent.component]!!
                    )
                )
            }

            entities[packetEntity.key]!!.component<Transform>().mutate()
        }
    }
}

class ClientConnectionSystem : NetworkConnectionSystem() {
    override fun connect(
        event: ConnectionEvent,
        entities: QueryView,
        eventQueue: NetworkEventQueue,
        lifecycle: EntitiesLifecycle
    ) {
    }

    override fun disconnect(
        event: ConnectionEvent,
        entities: QueryView,
        eventQueue: NetworkEventQueue,
        lifecycle: EntitiesLifecycle
    ) {
    }
}

class ClientRegistrationSystem : NetworkRegistrationSystem() {

    override fun executeNetwork(
        resources: ResourcesView,
        eventQueues: EventQueues<NetworkEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        super.executeNetwork(resources, eventQueues, query, lifecycle)

        query(setOf(Synchronized::class)).filter {
            val sync = it.component<Synchronized>().get()
            sync.registration == RegistrationStatus.Disconnected
        }.forEach {
            val synchronized = it.component<Synchronized>()

            synchronized.get().registration = RegistrationStatus.Sent
            synchronized.get().ownerEndpoint = eventQueues.network.getEndpoint()
            synchronized.mutate()
            eventQueues.network.queueLater(EntityRegistrationPacket(it))
        }
    }

    override fun register(
        registrationPacket: EntityRegistrationPacket,
        entities: QueryView,
        eventQueue: NetworkEventQueue,
        lifecycle: EntitiesLifecycle
    ) {
        if (entities[registrationPacket.id] != null) {
            val localSync = entities[registrationPacket.id]!!.component<Synchronized>()
            localSync.get().registration = RegistrationStatus.Registered
            localSync.get().ownerEndpoint = (registrationPacket.entity[Synchronized::class]!! as Synchronized).ownerEndpoint
            localSync.mutate()
            return
        }

        lifecycle.add(
            registrationPacket.entity.values.asSequence(),
            registrationPacket.id,
        )
    }
}