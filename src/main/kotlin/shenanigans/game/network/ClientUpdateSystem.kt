package shenanigans.game.network

import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntitiesLifecycle
import shenanigans.engine.ecs.QueryView
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.events.EventQueues
import shenanigans.engine.net.NetworkEventQueue
import shenanigans.engine.net.events.ConnectionEvent
import shenanigans.engine.physics.Collider
import shenanigans.engine.term.Logger
import shenanigans.engine.util.Transform
import kotlin.reflect.KClass

class ClientUpdateSystem : NetworkUpdateSystem() {
    override fun getUpdatePacket(
        components: Iterable<KClass<out Component>>,
        entities: QueryView,
        eventQueue: NetworkEventQueue
    ): EntityUpdatePacket {
        return EntityUpdatePacket(
            entities.filter {
                val synchronized = it.component<Synchronized>()

                synchronized.get().registration == RegistrationStatus.Registered &&
                        synchronized.get().ownerEndpoint == eventQueue.getEndpoint()
            }.map { entity ->
                entity.id to entity.entity.components.filter { component ->
                    components.contains(component.key)
                }.mapValues { it.value.component }
            }.toMap()
        )
    }

    override fun updateEntities(updatePacket: EntityUpdatePacket, entities: QueryView, eventQueue: NetworkEventQueue) {
        updatePacket.entities.forEach packet@{ packetEntity ->
            val entity = entities[packetEntity.key]

            if (entity == null) {
                Logger.warn("Entity Movement", "entity does not exist: " + packetEntity.key)
                return@packet
            }

            if (entity.component<Synchronized>().get().ownerEndpoint == eventQueue.getEndpoint()) {
                return@packet
            }

            entity.component<Transform>()
                .get().position.lerp(((packetEntity.value)[Transform::class]!! as Transform).position, 1f / 3f)
            entity.component<Transform>().mutate()

            entity.component<Collider>().get().polygon = (packetEntity.value[Collider::class]!! as Collider).polygon
            entity.component<Collider>().mutate()

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
            it.component<Synchronized>().get().registration == RegistrationStatus.Disconnected
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
            val entitySynchronization = entities[registrationPacket.id]!!.component<Synchronized>()
            entitySynchronization.get().registration =
                (registrationPacket.entity[Synchronized::class]!! as Synchronized).registration
            entitySynchronization.get().ownerEndpoint =
                (registrationPacket.entity[Synchronized::class]!! as Synchronized).ownerEndpoint
            entitySynchronization.mutate()

            Logger.log("Network System", "WHaHOOO: " + registrationPacket.id)
            return
        }

        lifecycle.add(
            registrationPacket.entity.values.asSequence(),
            registrationPacket.id,
        )

        Logger.log("Network System", "WahoOO!: " + registrationPacket.id)
    }
}