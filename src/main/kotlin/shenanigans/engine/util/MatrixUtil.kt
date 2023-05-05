package shenanigans.engine.util

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

private val tempQuaternion = Quaternionf()

fun Matrix4f.setToTransform(translation: Vector3f, rotation: Float, scale: Vector3f): Matrix4f {
    tempQuaternion.rotationZ(rotation)
    return this
        .translationRotateScale(
            translation, tempQuaternion, scale
        )
}

fun Matrix4f.setToTransform(transform: Transform): Matrix4f {
    return this.setToTransform(transform.position, transform.rotation, transform.scale)
}