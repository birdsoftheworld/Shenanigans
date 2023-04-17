package shenanigans.game.network

import shenanigans.engine.ecs.Component
import shenanigans.engine.net.MessageEndpoint
import shenanigans.engine.net.SendableClass
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import shenanigans.game.blocks.*
import java.util.*
import kotlin.reflect.KClass

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
        SendableClass(EntityUpdatePacket::class, instantiator = { EntityUpdatePacket(mapOf()) }),
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

fun synchronizedComponents(): Set<KClass<out Component>> {
    return setOf(
        Transform::class,
        Collider::class,
    )
}