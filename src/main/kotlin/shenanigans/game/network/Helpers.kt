package shenanigans.game.network

import shenanigans.engine.ecs.Component
import shenanigans.engine.net.MessageEndpoint
import shenanigans.engine.net.SendableClass
import shenanigans.game.level.block.*
import java.util.*

class Synchronized : Component {
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
        SendableClass(EntityDeRegistrationPacket::class, instantiator = { EntityDeRegistrationPacket(UUID.randomUUID()) }),
        SendableClass(RegistrationStatus::class),
        SendableClass(NormalBlock::class),
        SendableClass(OscillatingBlock::class),
        SendableClass(RespawnBlock::class),
        SendableClass(SlipperyBlock::class),
        SendableClass(SpikeBlock::class),
        SendableClass(TrampolineBlock::class),
        SendableClass(StickyBlock::class),
        SendableClass(Direction::class)
    )
}