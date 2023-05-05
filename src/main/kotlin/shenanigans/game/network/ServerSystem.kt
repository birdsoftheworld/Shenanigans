package shenanigans.game.server

import shenanigans.engine.ecs.EntitiesLifecycle
import shenanigans.engine.ecs.EntityView
import shenanigans.engine.ecs.QueryView
import shenanigans.engine.net.MessageDelivery
import shenanigans.engine.net.NetworkEventQueue
import shenanigans.engine.net.events.ConnectionEvent
import shenanigans.engine.term.Logger
import shenanigans.game.network.*

class ServerUpdateSystem : NetworkUpdateSystem() {

    override fun getUpdatePacket(
        components: Set<SynchronizedComponent>,
        entities: Sequence<EntityView>,
        eventQueue: NetworkEventQueue
    ): EntityUpdatePacket {
        return EntityUpdatePacket(
            entities.map { entity ->
                entity.id to entity.entity.components.filter { component ->
                    components.map {it.component }.contains(component.key)
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

            synchronizedComponents().forEach {synchronizedComponent ->
                entity.component(synchronizedComponent.component).replace(
                    synchronizedComponent.updateServer(
                        entity.component(synchronizedComponent.component).get(),
                        packetEntity.value[synchronizedComponent.component]!!
                    )
                )
            }
        }
    }
}

class ServerConnectionSystem : NetworkConnectionSystem() {
    override fun connect(
        event: ConnectionEvent,
        entities: QueryView,
        eventQueue: NetworkEventQueue,
        lifecycle: EntitiesLifecycle
    ) {
        entities.forEach {
            eventQueue.queueNetwork(
                EntityRegistrationPacket(it),
                delivery = MessageDelivery.ReliableOrdered,
                recipient = event.endpoint
            )
        }
    }

    override fun disconnect(
        event: ConnectionEvent,
        entities: QueryView,
        eventQueue: NetworkEventQueue,
        lifecycle: EntitiesLifecycle
    ) {
        entities
            .filter { it.component<Synchronized>().get().ownerEndpoint == event.endpoint }
            .forEach {
                eventQueue.queueNetwork(EntityDeRegistrationPacket(it.id))
                lifecycle.del(it.id)
            }
    }

}

class ServerRegistrationSystem : NetworkRegistrationSystem() {
    override fun register(
        registrationPacket: EntityRegistrationPacket,
        entities: QueryView,
        eventQueue: NetworkEventQueue,
        lifecycle: EntitiesLifecycle
    ) {
        if (entities[registrationPacket.id] != null) {
            Logger.warn("Entity Registration", "Received registration pack with duplicate ID " + registrationPacket.id)
        }

        (registrationPacket.entity[Synchronized::class] as Synchronized).registration = RegistrationStatus.Registered

        lifecycle.add(
            registrationPacket.entity.values.asSequence(),
            registrationPacket.id,
        )
        Logger.log("Entity Registration", registrationPacket.id.toString())
        eventQueue.queueNetwork(registrationPacket, MessageDelivery.ReliableOrdered)
    }
}