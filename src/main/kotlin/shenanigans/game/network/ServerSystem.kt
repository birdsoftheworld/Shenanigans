package shenanigans.game.server

import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntitiesLifecycle
import shenanigans.engine.ecs.QueryView
import shenanigans.engine.net.MessageDelivery
import shenanigans.engine.net.NetworkEventQueue
import shenanigans.engine.net.events.ConnectionEvent
import shenanigans.engine.physics.Collider
import shenanigans.engine.term.Logger
import shenanigans.engine.util.Transform
import shenanigans.game.network.*
import kotlin.reflect.KClass

class ServerUpdateSystem : NetworkUpdateSystem() {

    override fun getUpdatePacket(
        components: Iterable<KClass<out Component>>,
        entities: QueryView,
        eventQueue: NetworkEventQueue
    ): EntityUpdatePacket {
        return EntityUpdatePacket(
            entities.map { entity ->
                entity.id to entity.entity.components.filter { component ->
                    components.contains(component.key)
                }.mapValues { it.value.component }
            }.toMap()
        )
    }

    override fun updateEntities(updatePacket: EntityUpdatePacket, entities: QueryView, eventQueue: NetworkEventQueue) {
        updatePacket.entities.forEach { entity ->
            if (entities[entity.key] != null) {
                if (entities[entity.key]?.componentOpt<Transform>() == null) {
                    Logger.warn("Server Update", "entity does not have a transform!")
                }
                entities[entity.key]?.component<Transform>()!!.get().position =
                    ((entity.value)[Transform::class]!! as Transform).position
                entities[entity.key]?.component<Collider>()!!.get().polygon =
                    (entity.value[Collider::class]!! as Collider).polygon
                entities[entity.key]?.component<Collider>()!!.mutate()
            } else {
                Logger.warn("Entity Update", "entity does not exist: " + entity.key)
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