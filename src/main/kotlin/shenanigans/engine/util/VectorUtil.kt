package shenanigans.engine.util

import org.joml.*

fun Vector2ic.toFloat() = Vector2f(this.x().toFloat(), this.y().toFloat())

// not provided by JOML
fun Vector2fc.dot(a: Float, b: Float) = this.x() * a + this.y() * b

fun pointProjectionCollisionDistance(
    lineSegment: Pair<Vector2fc, Vector2fc>,
    point: Vector2fc,
    direction: Vector2fc
): Float {
    val worldToEmitter = Matrix3f(
        Matrix2f(
            rotate90Degrees(direction),
            direction,
        ).transpose()
    ).mul(Matrix3f().apply { setColumn(2, Vector3f(point.negate(Vector2f()), 1f)) })

    val endpoints = Pair(
        worldToEmitter.transformHomogenous(lineSegment.first),
        worldToEmitter.transformHomogenous(lineSegment.second)
    )

    val t = -endpoints.first.x() / (endpoints.second.x() - endpoints.first.x())

    return if (t in 0f..1f) {
        endpoints.first.y() + t * (endpoints.second.y() - endpoints.first.y())
    } else {
        Float.POSITIVE_INFINITY
    }
}

fun rotate90Degrees(vector: Vector2fc) = Vector2f(-vector.y(), vector.x())

fun Matrix3f.transformHomogenous(vector: Vector2fc): Vector2f {
    val homogenousImage = this.transform(Vector3f(vector, 1f))
    return Vector2f(homogenousImage.x(), homogenousImage.y()).div(homogenousImage.z())
}