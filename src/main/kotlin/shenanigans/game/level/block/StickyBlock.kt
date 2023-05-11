package shenanigans.game.level.block

import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.util.shapes.Polygon

class StickyBlock : Block() {
    override val solid = true
    override val visualShape = SQUARE_BLOCK_SHAPE
    override val colliderShape: Polygon = SQUARE_BLOCK_SHAPE
    override val texture = StickyBlock.texture

    companion object {
        val texture = TextureManager.createTexture(TextureKey("sticky"), "/sticky.png")
    }
}