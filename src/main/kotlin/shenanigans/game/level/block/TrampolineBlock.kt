package shenanigans.game.level.block

import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.util.shapes.Polygon

class TrampolineBlock : Block() {
    override val solid = true
    override val visualShape = SQUARE_BLOCK_SHAPE
    override val colliderShape: Polygon = SQUARE_BLOCK_SHAPE
    override val texture = TrampolineBlock.texture

    companion object {
        val texture = TextureManager.createTexture(TextureKey("trampoline"), "/spring.png")
    }
}