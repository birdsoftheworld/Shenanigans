package shenanigans.game.level.component

import shenanigans.engine.ecs.Component
import shenanigans.game.player.PlayerProperties

val MODIFIERS = mutableMapOf<String, (PlayerProperties) -> Unit>()

class PlayerModifier(val name: String) : Component {
    override fun equals(other: Any?): Boolean {
        return other is PlayerModifier && other.name == this.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}