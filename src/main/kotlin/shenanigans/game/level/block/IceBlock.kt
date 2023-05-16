package shenanigans.game.level.block

import org.joml.Vector3f
import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.util.shapes.Polygon
import shenanigans.game.level.component.ModifierId
import shenanigans.game.level.component.PlayerModifier
import shenanigans.game.level.component.SurfaceModifier

const val slipperyAccelerationX: Float = .4f
const val slipperyAirAccelerationX: Float = .4f

const val slipperyTurnSpeedX: Float = .2f
const val slipperyAirTurnSpeedX: Float = .3f // my kindness knows no bounds

const val slipperyDecelerationX: Float = .01f
const val slipperyWallSlideSpeedX: Float = 1.75f

class IceBlock : Block() {
    override val solid = true
    override val visualShape = SQUARE_BLOCK_SHAPE
    override val colliderShape: Polygon = SQUARE_BLOCK_SHAPE
    override val texture = IceBlock.texture

    override fun toComponents(pos: Vector3f): Sequence<Component> {
        return super.toComponents(pos).plus(
            SurfaceModifier(
                PlayerModifier(ModifierId("ice"), true),
                PlayerModifier(ModifierId("wallIce"), true)
            )
        )
    }

    companion object {
        val texture = TextureManager.createTexture(TextureKey("ice"), "/ice.png")
    }
}