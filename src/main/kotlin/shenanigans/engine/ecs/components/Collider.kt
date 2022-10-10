package shenanigans.engine.ecs.components

import org.joml.Vector2d
import org.joml.Vector2f
import shenanigans.engine.ecs.Component

class Collider constructor(val vertices: Array<Vector2f>, val static: Boolean, val triggerCollider: Boolean) :
    Component {
    constructor(shape: Shape, static: Boolean, triggerCollider: Boolean) :
            this(shape.vertices, static, triggerCollider)
}