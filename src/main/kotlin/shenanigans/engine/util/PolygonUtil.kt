package shenanigans.engine.util

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector4f

fun Polygon.isPointInside(point: Vector2fc, transform: Transform) : Boolean{
    val transformMatrix = Matrix4f()

    transformMatrix.setToTransform(transform.position, transform.rotation, transform.scale)

    val transformedVertices = Array(vertices.size) { Vector2f() }

    for (i in vertices.indices) {
        val vertex = Vector4f(vertices[i], 0f, 1f).mul(transformMatrix)
        transformedVertices[i].x = vertex.x
        transformedVertices[i].y = vertex.y
    }

    var count = 0
    for (i in vertices.indices) {
        if(pointProjectionIntersectsLine(
                point,
                Pair(transformedVertices[i], transformedVertices[(i + 1) % vertices.size]))
        ) {
            count ++
        }
    }
    return count % 2 == 1
}

private fun pointProjectionIntersectsLine(point: Vector2fc, line: Pair<Vector2fc, Vector2fc>) : Boolean {
    return ((((line.first.y() > point.y() && line.second.y() < point.y()) ||
            (line.first.y() < point.y() && line.second.y() > point.y()))) &&
            (point.x() < line.first.x() || point.x() < line.second.x()))
}