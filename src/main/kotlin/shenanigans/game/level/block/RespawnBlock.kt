package shenanigans.game.level.block

class RespawnBlock : Block() {
    override val solid = false
    override val shape = SQUARE_BLOCK_SHAPE
    override val texture = NULL_TEXTURE
}