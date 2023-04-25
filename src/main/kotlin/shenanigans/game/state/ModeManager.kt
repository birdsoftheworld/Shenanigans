package shenanigans.game.state

import shenanigans.engine.ecs.Component

enum class Mode {
    PLACE,
    PLAY
}

class ModeManager : Component {
    var mode: Mode = Mode.PLAY
}