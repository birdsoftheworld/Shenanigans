package shenanigans.game.network

import shenanigans.engine.ecs.Component
import shenanigans.engine.net.MessageEndpoint
import shenanigans.engine.net.SendableClass
import java.util.*

class Synchronized : Component {
    @Transient
    var registration = RegistrationStatus.Disconnected
    var ownerEndpoint : MessageEndpoint? = null
}

enum class RegistrationStatus {
    Disconnected,
    Sent,
    Registered
}

fun sendables(): Set<SendableClass<Any>> {
    return setOf(
        SendableClass(EntityMovementPacket::class, instantiator = { EntityMovementPacket(mapOf()) }),
        SendableClass(
            EntityRegistrationPacket::class,
            instantiator = { EntityRegistrationPacket(UUID.randomUUID(), mapOf()) }),
        SendableClass(EntityDeRegistrationPacket::class, instantiator = { EntityDeRegistrationPacket(UUID.randomUUID()) })
    )
}