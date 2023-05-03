package shenanigans.game.level.block

import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager

class IceBlock : Block() {
    override val solid = true
    override val shape = SQUARE_BLOCK_SHAPE
    override val texture = IceBlock.texture

    companion object {
        val texture = TextureManager.createTexture(TextureKey("ice"), "/ice.png")
    }
}