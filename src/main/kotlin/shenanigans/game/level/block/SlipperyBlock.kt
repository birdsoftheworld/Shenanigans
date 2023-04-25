package shenanigans.game.level.block

class SlipperyBlock : Block() {
    override val solid = true
    override val shape = SQUARE_BLOCK_SHAPE
    override val texture = NULL_TEXTURE
}