package shenanigans.engine.util

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector2ic

fun Vector2ic.toFloat() = Vector2f(this.x().toFloat(), this.y().toFloat())

// not provided by JOML
fun Vector2fc.dot(a: Float, b: Float) = this.x() * a + this.y() * b

fun pointProjectionCollisionDistance(lineSegment : Pair<Vector2fc, Vector2fc>, point : Vector2fc, direction : Vector2fc) : Float {
    val p0 : Vector2f = lineSegment.first.sub(point, Vector2f())
    val p1 : Vector2f = lineSegment.second.sub(point, Vector2f())
    val t = direction.dot(p0) / direction.dot(p0.add(p1, Vector2f()))

    if(t < 0 || t > 1) {
        return -1f
    }

    val collisionPoint = p0.mul(1-t).add(p1.mul(t))

    return direction.normalize(Vector2f()).dot(collisionPoint)
}