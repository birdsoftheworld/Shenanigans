package shenanigans.engine.util

import org.joml.Matrix4f
import org.joml.Vector3f

fun Matrix4f.setToTransform(translation: Vector3f, rotation: Float, scale: Vector3f): Matrix4f {
    return this
        .translationRotateScale(
            translation.x, translation.y, translation.z, 0f, 0f, rotation, 1f, scale.x, scale.y, scale.z
        )
}

fun Matrix4f.setToTransform(transform: Transform): Matrix4f {
    return this.setToTransform(transform.position, transform.rotation, transform.scale)
}