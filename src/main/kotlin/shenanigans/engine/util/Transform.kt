package shenanigans.engine.util

import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import java.util.Vector

class Transform(var position: Vector2f, var rotation: Float, var scale: Vector2f): Component {
    constructor() : this(Vector2f(), 0f, Vector2f(1f, 1f))
    constructor(position: Vector2f) : this(position, 0f, Vector2f(1f, 1f))
}