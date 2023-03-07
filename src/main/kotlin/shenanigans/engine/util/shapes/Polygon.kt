package shenanigans.engine.util.shapes

import org.joml.Vector2f

// please don't mutate
open class Polygon(val vertices: Array<Vector2f>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Polygon

        if (!vertices.contentEquals(other.vertices)) return false

        return true
    }

    override fun hashCode(): Int {
        return vertices.contentHashCode()
    }
}