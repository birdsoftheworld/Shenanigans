package shenanigans.game.level.block

class StickyBlock : Block() {
    override val solid = true
    override val shape = SQUARE_BLOCK_SHAPE
    override val texture = STICKY_TEXTURE
}