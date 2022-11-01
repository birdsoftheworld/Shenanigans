package shenanigans.engine.util

import org.joml.Matrix4f
import org.joml.Vector2f

class OrthoCamera {
    private val projectionMatrix = Matrix4f()
    private val viewMatrix = Matrix4f()
    private val modelViewMatrix = Matrix4f()

    private val translation = Vector2f()
    private val rotation = 0f

    private var screenWidth = -1
    private var screenHeight = -1

    fun setScreenSize(w: Int, h: Int) {
        screenWidth = w
        screenHeight = h
    }

    fun getProjectionMatrix(): Matrix4f {
        return projectionMatrix
            .identity()
            .ortho2D(0f, screenWidth.toFloat(), screenHeight.toFloat(), 0f)
    }

    fun getViewMatrix(): Matrix4f {
        return viewMatrix
            .identity()
            .rotate(
                rotation,
                0f,
                0f,
                1f
            )
            .translate(-translation.x, -translation.y, 0f)
    }

    fun getModelViewMatrix(translation: Vector2f, rotation: Float, scale: Vector2f, viewMatrix: Matrix4f): Matrix4f {
        modelViewMatrix
            .identity()
            .translate(translation.x, translation.y, 0f)
            .rotateZ(rotation)
            .scale(scale.x, scale.y, 1f)
        return Matrix4f(viewMatrix).mul(modelViewMatrix)
    }
}