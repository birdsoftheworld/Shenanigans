package shenanigans.engine.util

import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import java.util.Vector

class Transform(val position: Vector2f, val rotation: Float, val scale: Vector2f): Component {
    constructor() : this(Vector2f(), 0f, Vector2f(1f, 1f))
    constructor(position: Vector2f) : this(position, 0f, Vector2f(1f, 1f))
}