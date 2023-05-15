package shenanigans.game.level.block

import org.joml.Vector3f
import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.util.shapes.Polygon
import shenanigans.game.level.component.PlayerModifier

const val slipperyMovementMultiplier: Float = .4f
const val slipperyTurnSpeedMultiplier: Float = .2f
const val slipperyDecelerationMultiplier: Float = .01f

class IceBlock : Block() {
    override val solid = true
    override val visualShape = SQUARE_BLOCK_SHAPE
    override val colliderShape: Polygon = SQUARE_BLOCK_SHAPE
    override val texture = IceBlock.texture

    override fun toComponents(pos: Vector3f): Sequence<Component> {
        return super.toComponents(pos).plus(PlayerModifier("ice"))
    }

    companion object {
        val texture = TextureManager.createTexture(TextureKey("ice"), "/ice.png")
    }
}