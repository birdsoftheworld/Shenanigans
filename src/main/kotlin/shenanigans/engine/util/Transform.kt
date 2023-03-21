package shenanigans.engine.util

import org.joml.Vector2f
import org.joml.Vector3f
import shenanigans.engine.ecs.Component

data class Transform(var position: Vector3f = Vector3f(), var rotation: Float = 0f, var scale: Vector3f = Vector3f(1f, 1f, 1f)) : Component {
    constructor(position: Vector2f, rotation: Float = 0f, scale: Vector2f = Vector2f(1f, 1f)) : this(Vector3f(position, 0f), rotation, Vector3f(scale, 1f))
}