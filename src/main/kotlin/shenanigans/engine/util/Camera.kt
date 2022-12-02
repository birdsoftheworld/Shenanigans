package shenanigans.engine.util

import org.joml.Matrix4f
import org.joml.Vector2f

abstract class Camera {
    protected val projectionMatrix = Matrix4f()
    protected val viewMatrix = Matrix4f()
    protected val modelViewMatrix = Matrix4f()

    protected val translation = Vector2f()
    protected val rotation = 0f

    protected var screenWidth = -1
    protected var screenHeight = -1

    fun setScreenSize(w: Int, h: Int) {
        screenWidth = w
        screenHeight = h
    }

    abstract fun computeProjectionMatrix(): Matrix4f

    fun computeViewMatrix(): Matrix4f {
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

    fun computeModelViewMatrix(translation: Vector2f, rotation: Float, scale: Vector2f, viewMatrix: Matrix4f): Matrix4f {
        modelViewMatrix.setToTransform(translation, rotation, scale)
        return Matrix4f(viewMatrix).mul(modelViewMatrix)
    }
}