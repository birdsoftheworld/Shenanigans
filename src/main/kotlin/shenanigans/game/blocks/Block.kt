package shenanigans.game.blocks

import shenanigans.engine.ecs.Component

abstract class Block : Component {
    open val solid: Boolean = true
}