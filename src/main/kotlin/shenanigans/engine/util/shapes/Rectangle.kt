package shenanigans.engine.util.shapes

import org.joml.Vector2f
import org.joml.Vector2fc

class Rectangle(val width: Float, val height: Float) : Polygon(
    arrayOf(
        Vector2f(0f, 0f),
        Vector2f(0f, height),
        Vector2f(width, height),
        Vector2f(width, 0f)
    )
) {
    val widthByHeight: Vector2fc = Vector2f(width, height)
}