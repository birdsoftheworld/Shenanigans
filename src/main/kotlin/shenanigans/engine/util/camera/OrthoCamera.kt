package shenanigans.engine.util.camera

import org.joml.Matrix4f

class OrthoCamera : Camera() {
    override fun computeProjectionMatrix(): Matrix4f {
        return projectionMatrix
            .setOrtho(0f, screenWidth.toFloat(), screenHeight.toFloat(), 0f, -1000f, 1000f)
    }
}