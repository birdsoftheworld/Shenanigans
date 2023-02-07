package shenanigans.engine.util

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector2f

private val tempQua = Quaternionf()
fun Matrix4f.setToTransform(translation: Vector2f, rotation: Float, scale: Vector2f): Matrix4f {
    tempQua.rotationZ(rotation)
    return this
        .translationRotateScale(
            translation.x, translation.y, 0f, tempQua.x, tempQua.y, tempQua.z, tempQua.w, scale.x, scale.y, 1f
        )
}

fun Matrix4f.setToTransform(transform: Transform): Matrix4f {
    return this.setToTransform(transform.position, transform.rotation, transform.scale)
}