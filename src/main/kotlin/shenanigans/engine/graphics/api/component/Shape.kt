package shenanigans.engine.graphics.api.component

import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.api.Color

/**
 * a shape to be rendered by `ShapeSystem`
 */
data class Shape(
    val vertices : Array<Vector2f>,
    val color: Color,
) : Component {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Shape

        if (!vertices.contentEquals(other.vertices)) return false

        return true
    }

    fun width(): Float{
        return this.vertices[2].x -this.vertices[0].x
    }

    override fun hashCode(): Int {
        return vertices.contentHashCode()
    }
}