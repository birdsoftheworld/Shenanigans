package shenanigans.engine.physics

import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.api.component.Shape

class Collider constructor(
        val vertices: Array<Vector2f>,
        val static: Boolean,
        val triggerCollider: Boolean
        ) : Component {
    var transformedVertices = Array(vertices.size) { Vector2f() }

    constructor(shape: Shape, static: Boolean) :
            this(shape.vertices, static, false)
}