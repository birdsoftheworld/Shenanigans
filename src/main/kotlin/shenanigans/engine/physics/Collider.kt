package shenanigans.engine.physics

import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import shenanigans.engine.util.shapes.Polygon

class Collider constructor(
    var polygon: Polygon,
    val static: Boolean,
    val triggerCollider: Boolean = false,
    val tracked: Boolean = false
    ) : Component {
    var transformedVertices = Array(polygon.vertices.size) { Vector2f() }
}