package shenanigans.game.level.block

import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager

class RespawnBlock : Block() {
    override val solid = false
    override val shape = SQUARE_BLOCK_SHAPE
    override val texture = RespawnBlock.texture

    companion object {
        val texture = TextureManager.createTexture(TextureKey("respawn"), "/respawn.png")
    }
}