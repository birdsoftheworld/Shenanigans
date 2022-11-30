package shenanigans.engine.events.control

import shenanigans.engine.ecs.System
import shenanigans.engine.events.Event
import shenanigans.engine.scene.Scene

sealed class ControlEvent : Event

object ExitEvent : ControlEvent()

data class UpdateDefaultSystemsEvent(val update: (MutableList<System>) -> Unit) : ControlEvent()

data class SceneChangeEvent(val scene: Scene) : ControlEvent()