package shenanigans.game.level.block

import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.util.shapes.Polygon

class AccelerationBlock : Block() {
    override val solid = false
    override val visualShape = SQUARE_BLOCK_SHAPE
    override val colliderShape: Polygon = SQUARE_BLOCK_SHAPE
    override val texture = AccelerationBlock.texture

    companion object {
        val texture = TextureManager.createTexture(TextureKey("trampoline"), "/acceleration.png")
    }
}