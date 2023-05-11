package shenanigans.engine.util.shapes

import org.joml.Vector2f
import org.joml.Vector2fc

class Rectangle(val width: Float, val height: Float, val offsetX: Float = 0f, val offsetY: Float = 0f) : Polygon(
    listOf(
        Vector2f(offsetX, offsetY),
        Vector2f(offsetX, offsetY + height),
        Vector2f(offsetX + width, offsetY + height),
        Vector2f(offsetX + width, offsetY)
    )
) {
    override fun offset(x: Float, y: Float): Rectangle {
        return Rectangle(this.width, this.height, this.offsetX + x, this.offsetY + y)
    }

    val widthByHeight: Vector2fc = Vector2f(width, height)
}