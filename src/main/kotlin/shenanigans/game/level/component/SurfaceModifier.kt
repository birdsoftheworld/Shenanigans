package shenanigans.game.level.component

import shenanigans.engine.ecs.Component
import shenanigans.game.player.PlayerProperties

@JvmInline
value class ModifierId(val id: String)

val MODIFIERS = mutableMapOf<ModifierId, (PlayerProperties) -> Unit>()

// `keepDuringCoyoteTime` exists because otherwise modifiers couldn't affect properties during coyote time
data class PlayerModifier(val id: ModifierId, val keepOnJump: Boolean, val keepDuringCoyoteTime: Boolean = keepOnJump)

class SurfaceModifier(val floor: PlayerModifier? = null, val wall: PlayerModifier? = null) : Component