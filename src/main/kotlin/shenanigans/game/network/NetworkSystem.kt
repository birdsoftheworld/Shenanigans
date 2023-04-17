package shenanigans.game.network

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.net.NetworkEventQueue
import shenanigans.engine.term.Logger
import shenanigans.engine.util.Transform
import kotlin.reflect.KClass

class NetworkSystem : System {

    override fun executeNetwork(
        resources: ResourcesView,
        eventQueues: EventQueues<NetworkEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(setOf(Synchronized::class))

        eventQueues.network.receive(EntityDeRegistrationPacket::class).forEach {
            lifecycle.del(it.id)
        }

        eventQueues.own.receive(EntityMovementPacket::class).forEach update@{ packet ->
            val entities = query(setOf(Synchronized::class))
            packet.entities.forEach packet@{ packetEntity ->
                val entity = entities[packetEntity.key]

                if (entity == null) {
                    Logger.warn("Entity Movement", "entity does not exist: " + packetEntity.key)
                    return@packet
                }

                if (entity.component<Synchronized>().get().ownerEndpoint == eventQueues.network.getEndpoint()) {
                    return@packet
                }

                val position = entity.component<Transform>().get().position
                position.lerp((packetEntity.value).position, 1f / 3f)

                entities[packetEntity.key]!!.component<Transform>().mutate()
            }
        }

        eventQueues.own.receive(EntityRegistrationPacket::class).forEach registration@{ packet ->
            if (entities[packet.id] != null) {
                val entitySynchronization = entities[packet.id]!!.component<Synchronized>()
                entitySynchronization.get().registration = (packet.entity[Synchronized::class]!! as Synchronized).registration
                entitySynchronization.get().ownerEndpoint = (packet.entity[Synchronized::class]!! as Synchronized).ownerEndpoint
                entitySynchronization.mutate()

                Logger.log("Network System", "WHaHOOO: " + packet.id)
                return@registration
            }

            lifecycle.add(
                packet.entity.values.asSequence(),
                packet.id,
            )

            Logger.log("Network System", "WahoOO!: " + packet.id)
        }

        entities.filter {
                it.component<Synchronized>().get().registration == RegistrationStatus.Disconnected
        }.forEach {
            val synchronized = it.component<Synchronized>()

            synchronized.get().registration = RegistrationStatus.Sent
            synchronized.get().ownerEndpoint = eventQueues.network.getEndpoint()
            synchronized.mutate()
            eventQueues.network.queueLater(EntityRegistrationPacket(it))
        }

        eventQueues.network.queueLater(EntityMovementPacket(entities.filter {
            val synchronized = it.component<Synchronized>()

            synchronized.get().registration == RegistrationStatus.Registered &&
            synchronized.get().ownerEndpoint == eventQueues.network.getEndpoint()
        }))
    }
}