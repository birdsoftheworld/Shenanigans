package shenanigans.engine.util

import org.joml.Matrix4f
import org.joml.Vector2f

class OrthoCamera {
    private val projectionMatrix = Matrix4f()
    private val worldMatrix = Matrix4f()

    fun getProjectionMatrix(screenWidth: Int, screenHeight: Int): Matrix4f {
        return projectionMatrix
            .identity()
            .ortho2D(0f, screenWidth.toFloat(), screenHeight.toFloat(), 0f)
    }

    fun getWorldMatrix(translation: Vector2f, rotation: Float, scale: Vector2f): Matrix4f {
        return worldMatrix
            .identity()
            .translate(translation.x, translation.y, 0f)
            .rotateZ(rotation)
            .scale(scale.x, scale.y, 1f)
    }
}