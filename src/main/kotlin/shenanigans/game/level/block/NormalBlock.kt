package shenanigans.game.level.block

import shenanigans.engine.util.shapes.Rectangle

class NormalBlock(override val shape: Rectangle) : Block() {
    override val solid = true
    override val texture = NORMAL_TEXTURE
}