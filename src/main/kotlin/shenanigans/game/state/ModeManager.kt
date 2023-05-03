package shenanigans.game.state

import shenanigans.engine.ecs.Component

enum class Mode {
    BUILD,
    RUN
}

class ModeManager : Component {
    var mode: Mode = Mode.RUN
}