package shenanigans.engine.graphics.api

import org.joml.Vector2f
import shenanigans.engine.ecs.Component

/**
 * a shape to be rendered by `ShapeSystem`
 */
data class Shape(
    val vertices : Array<Vector2f>,
    val color: Color,
    val texture: Texture,
) : Component {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Shape

        if (!vertices.contentEquals(other.vertices)) return false

        return true
    }

    override fun hashCode(): Int {
        return vertices.contentHashCode()
    }
}