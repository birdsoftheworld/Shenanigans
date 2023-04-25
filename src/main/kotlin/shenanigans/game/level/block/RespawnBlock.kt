package shenanigans.game.level.block

import shenanigans.engine.util.shapes.Polygon
import shenanigans.engine.util.shapes.Rectangle

class RespawnBlock : Block() {
    override val solid = false
    override val shape = SQUARE_BLOCK_SHAPE
    override val texture = NULL_TEXTURE
}