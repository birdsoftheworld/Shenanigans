package shenanigans.engine.util.camera

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector4f
import shenanigans.engine.util.setToTransform

abstract class Camera {
    protected val projectionMatrix = Matrix4f()
    protected val viewMatrix = Matrix4f()
    protected val modelViewMatrix = Matrix4f()

    val translation = Vector3f()
    val scaling = Vector3f(1f, 1f, 1f)
    var rotation = 0f

    var screenWidth = -1
        protected set
    var screenHeight = -1
        protected set

    private val _tempVec = Vector4f()
    private val _tempMat = Matrix4f()
    private val _tempQua = Quaternionf()

    fun setScreenSize(w: Int, h: Int) {
        screenWidth = w
        screenHeight = h
    }

    fun reset(): Camera {
        translation.set(0f, 0f, 0f)
        rotation = 0f
        scaling.set(1f, 1f, 1f)
        return this
    }

    fun rotate(rotation: Float): Camera {
        this.rotation += rotation
        return this
    }

    fun scale(vector3f: Vector3f): Camera {
        scaling.mul(vector3f)
        return this
    }

    fun translate(x: Float, y: Float, z: Float = 0f): Camera {
        translation.add(x, y, z)
        return this
    }

    fun transformPoint(vector3f: Vector3f): Vector3f {
        _tempVec.set(vector3f, 1f).mul(this.computeViewMatrix())
        return vector3f.set(_tempVec.x / _tempVec.w, _tempVec.y / _tempVec.w, _tempVec.z / _tempVec.w)
    }

    fun untransformPoint(vector3f: Vector3f): Vector3f {
        _tempMat.set(this.computeViewMatrix())
        _tempVec.set(vector3f, 1f).mul(_tempMat.invert())
        return vector3f.set(_tempVec.x / _tempVec.w, _tempVec.y / _tempVec.w, _tempVec.z / _tempVec.w)
    }

    abstract fun computeProjectionMatrix(): Matrix4f

    fun computeViewMatrix(): Matrix4f {
        _tempQua.rotationZ(rotation)
        return viewMatrix
            .translationRotateScale(
                -translation.x, -translation.y, 0f,
                _tempQua.x, _tempQua.y, _tempQua.z, _tempQua.w,
                scaling.x, scaling.y, 1f
            )
    }

    fun computeModelViewMatrix(
        translation: Vector3f,
        rotation: Float,
        scale: Vector3f,
        viewMatrix: Matrix4f
    ): Matrix4f {
        modelViewMatrix.setToTransform(translation, rotation, scale)
        return Matrix4f(viewMatrix).mul(modelViewMatrix)
    }
}