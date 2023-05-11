package shenanigans.engine.util.shapes

import org.joml.Vector2f
import org.joml.Vector2fc

// please don't mutate
open class Polygon(val vertices: List<Vector2fc>) {
    open fun offset(x: Float, y: Float = x): Polygon {
        return Polygon(vertices.map { it.add(x, y, Vector2f()) })
    }
}