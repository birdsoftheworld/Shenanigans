package shenanigans.game

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.game.level.block.*
import shenanigans.game.level.component.MODIFIERS
import shenanigans.game.level.component.ModifierId
import kotlin.reflect.KClass

object RegistrySystem : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        MODIFIERS[ModifierId("ice")] = {
            it.maxAcceleration *= slipperyAccelerationX
            it.maxAirAcceleration *= slipperyAirAccelerationX

            it.maxTurnSpeed *= slipperyTurnSpeedX
            it.maxAirTurnSpeed *= slipperyAirTurnSpeedX

            it.maxDeceleration *= slipperyDecelerationX
        }

        MODIFIERS[ModifierId("wallIce")] = {
            it.wallSlideSpeed *= slipperyWallSlideSpeedX
        }

        MODIFIERS[ModifierId("sticky")] = {
            it.jumpSpeed *= stickyFloorJumpHeightX
            it.maxSpeed *= stickyMaxSpeedX
            it.wallJumpHSpeed *= stickyWallJumpHSpeedX
            it.wallJumpVSpeed *= stickyWallJumpVSpeedX
            it.wallSlideSpeed *= stickyWallJumpSlideSpeedX
        }
    }
}