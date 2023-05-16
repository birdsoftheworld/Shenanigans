package shenanigans.game.level.block

import org.joml.Vector3f
import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.util.shapes.Polygon
import shenanigans.game.level.component.ModifierId
import shenanigans.game.level.component.PlayerModifier
import shenanigans.game.level.component.SurfaceModifier

const val stickyFloorJumpHeightX: Float = .55f
const val stickyMaxSpeedX: Float = 1f
const val stickyWallJumpHSpeedX: Float = .9f
const val stickyWallJumpVSpeedX: Float = .75f
const val stickyWallJumpSlideSpeedX: Float = .85f

class StickyBlock : Block() {
    override val solid = true
    override val visualShape = SQUARE_BLOCK_SHAPE
    override val colliderShape: Polygon = SQUARE_BLOCK_SHAPE
    override val texture = StickyBlock.texture

    override fun toComponents(pos: Vector3f): Sequence<Component> {
        val sticky = PlayerModifier(ModifierId("sticky"), keepOnJump = false, keepDuringCoyoteTime = true)
        return super.toComponents(pos).plus(
            SurfaceModifier(
                sticky,
                sticky
            )
        )
    }

    companion object {
        val texture = TextureManager.createTexture(TextureKey("sticky"), "/sticky.png")
    }
}