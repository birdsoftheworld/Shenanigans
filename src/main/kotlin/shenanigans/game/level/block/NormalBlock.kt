package shenanigans.game.level.block

import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.util.shapes.Rectangle

class NormalBlock(override val shape: Rectangle) : Block() {
    override val solid = true
    override val texture = NormalBlock.texture

    companion object {
        val texture = TextureManager.createTexture(TextureKey("normal"), "/normal.png")
    }
}