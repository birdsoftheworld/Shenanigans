package shenanigans.engine.util

import org.joml.Matrix4f
import org.joml.Vector2f

fun Matrix4f.setToTransform(translation: Vector2f, rotation: Float, scale: Vector2f): Matrix4f {
    return this
        .identity()
        .translate(translation.x, translation.y, 0f)
        .rotateZ(rotation)
        .scale(scale.x, scale.y, 1f)
}

fun Matrix4f.setToTransform(transform: Transform): Matrix4f {
    return this.setToTransform(transform.position, transform.rotation, transform.scale)
}