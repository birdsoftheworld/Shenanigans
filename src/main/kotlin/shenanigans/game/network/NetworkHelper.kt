package shenanigans.game.network

import shenanigans.engine.ecs.Component

class Synchronized : Component {
    val connected : Boolean = false
}

annotation class ClientOnly