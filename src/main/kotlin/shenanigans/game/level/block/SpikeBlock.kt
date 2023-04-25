package shenanigans.game.level.block

import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager

class SpikeBlock : Block() {
    override val solid = false
    override val shape = SQUARE_BLOCK_SHAPE
    override val texture = SPIKE_TEXTURE

    companion object {
        val texture = TextureManager.createTexture(TextureKey("hole"), "/hole.png")
    }
}