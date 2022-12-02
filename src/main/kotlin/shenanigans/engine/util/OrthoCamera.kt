package shenanigans.engine.util

import org.joml.Matrix4f

class OrthoCamera : Camera() {
    override fun computeProjectionMatrix(): Matrix4f {
        return projectionMatrix
            .identity()
            .ortho2D(0f, screenWidth.toFloat(), screenHeight.toFloat(), 0f)
    }
}