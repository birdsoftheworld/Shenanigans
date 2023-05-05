package shenanigans.game.network

import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.net.MessageEndpoint
import shenanigans.engine.net.SendableClass
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import shenanigans.engine.util.shapes.Rectangle
import shenanigans.game.level.block.*
import java.util.*
import kotlin.reflect.KClass

class Synchronized : Component {
    var registration = RegistrationStatus.Disconnected
    var ownerEndpoint: MessageEndpoint? = null
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
        SendableClass(
            EntityDeRegistrationPacket::class,
            instantiator = { EntityDeRegistrationPacket(UUID.randomUUID()) }),
        SendableClass(RegistrationStatus::class),
        SendableClass(NormalBlock::class, instantiator = { NormalBlock(Rectangle(0f, 0f)) }),
        SendableClass(NormalBlock::class),
        SendableClass(OscillatingBlock::class),
        SendableClass(RespawnBlock::class),
        SendableClass(IceBlock::class),
        SendableClass(SpikeBlock::class),
        SendableClass(TrampolineBlock::class),
        SendableClass(StickyBlock::class),
        SendableClass(Direction::class),
        SendableClass(Array<Vector2f>::class)
    )
}

class SynchronizedComponent(
    val component: KClass<out Component>,
    val updateClient: ((Component, Component) -> Component) = { _: Component, updated: Component -> updated },
    val updateServer: ((Component, Component) -> Component) = { _: Component, updated: Component -> updated }
)

fun synchronizedComponents(): Set<SynchronizedComponent> {
    return setOf(
        SynchronizedComponent(Transform::class, updateClient = {
                initial: Component, updated : Component ->
            (initial as Transform).position.lerp((updated as Transform).position, 1f / 3f)
            return@SynchronizedComponent initial
        }),
        SynchronizedComponent(Collider::class),
        SynchronizedComponent(Sprite::class),
    )
}