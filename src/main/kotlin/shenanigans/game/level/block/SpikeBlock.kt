package shenanigans.game.level.block

import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.util.shapes.Polygon

class SpikeBlock : Block() {
    override val solid = false
    override val visualShape = SQUARE_BLOCK_SHAPE
    override val colliderShape: Polygon = SLIGHTLY_SMALLER_SQUARE
    override val texture = SpikeBlock.texture

    companion object {
        val texture = TextureManager.createTexture(TextureKey("spike"), "/spike.png")
    }
}