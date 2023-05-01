package shenanigans.game.level.block

import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager

class StickyBlock : Block() {
    override val solid = true
    override val shape = SQUARE_BLOCK_SHAPE
    override val texture = StickyBlock.texture

    companion object {
        val texture = TextureManager.createTexture(TextureKey("sticky"), "/sticky.png")
    }
}