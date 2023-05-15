package shenanigans.game.level.block

import shenangians.game.level.component.PlayerModifier

abstract class ModifierBlock : Block {
    abstract val modifier: Pair<String, (PlayerProperties) -> Unit>

    override fun toComponents(pos: Vector3f): Sequence<Component> {
        return super.toComponents(pos).plus(PlayerModifier(modifier.first))
    }
}