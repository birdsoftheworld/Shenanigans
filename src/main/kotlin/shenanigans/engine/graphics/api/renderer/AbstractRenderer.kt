package shenanigans.engine.graphics.api.renderer

import org.joml.Matrix4f
import org.joml.Vector4f
import shenanigans.engine.graphics.Mesh
import shenanigans.engine.graphics.VertexAttribute
import shenanigans.engine.graphics.shader.Shader

abstract class AbstractRenderer(attribs: Set<VertexAttribute>, val vertexCapacity: Int = DEFAULT_MAX_VERTICES, val indicesCapacity: Int = DEFAULT_MAX_INDICES) {

    protected companion object {
        const val DEFAULT_MAX_VERTICES = 1024
        const val DEFAULT_MAX_INDICES = 1024
    }

    protected abstract val shader: Shader

    protected val mesh = Mesh(vertexCapacity, indicesCapacity, attribs)
    protected var started = false

    protected val indices = ArrayList<Int>(vertexCapacity)
    protected val positions = ArrayList<Float>(indicesCapacity * 3)
    protected var lowestIndex = 0

    protected var nVertices = 0
    protected var nIndices = 0

    /**
     * the projection matrix, used for projecting all vertices once `end()` is called
     */
    var projection = Matrix4f()
        set(value) {
            projection.set(value)
        }

    /**
     * the transformation matrix, used for transforming any drawing calls such as `rect()`
     */
    var transformation = Matrix4f()
        set(value) {
            transformation.set(value)
        }

    private val _temp = Vector4f()

    protected fun addIndex(index: Int) {
        indices.add(index + lowestIndex)
        nIndices++
    }

    protected fun addVertex(x: Float, y: Float, z: Float) {
        _temp.set(x, y, z, 1f).mul(transformation)
        positions.add(_temp.x / _temp.w)
        positions.add(_temp.y / _temp.w)
        positions.add(_temp.z / _temp.w)
        lowestIndex++
        nVertices++
    }

    /**
     * start rendering
     * must be called before end()
     */
    fun start() {
        if (started) throw IllegalStateException("Must end rendering before starting")
        started = true
    }

    /**
     * stop rendering
     * must be called after start()
     */
    fun end() {
        if (!started) throw IllegalStateException("Must start rendering before ending")

        this.writeToMesh()

        this.render()

        this.clear()

        started = false
    }

    protected fun writeToMesh() {
        mesh.writeIndices(indices.toIntArray())
        mesh.writeData(VertexAttribute.POSITION, positions.toFloatArray())
        this.writeVertexAttributes()
    }

    protected fun clear() {
        indices.clear()
        positions.clear()
        this.clearVertexAttributes()

        lowestIndex = 0
        nVertices = 0
        nIndices = 0
    }

    protected abstract fun writeVertexAttributes()
    protected abstract fun clearVertexAttributes()

    protected abstract fun createUniforms()
    protected abstract fun setUniforms()

    fun discard() {
        mesh.discard()
    }

    protected open fun render() {
        shader.bind()
        setUniforms()
        mesh.render()
        shader.unbind()
    }

    protected fun flush() {
        if(lowestIndex != 0) {
            this.writeToMesh()
            this.render()
        }

        this.clear()
    }

    protected fun flushIfFull(newVertices: Int, newIndices: Int) {
        if(newIndices + nIndices >= indicesCapacity || newVertices + nVertices >= vertexCapacity) {
            flush()
        }
    }
}