package shenanigans.engine.graphics

import org.lwjgl.opengl.GL30C.*

const val VERTICES_INDEX = 0
const val COLORS_INDEX = 1

// must be greater than the highest vertex attribute,
// in order to keep vboIds and vertexAttribs in sync
// probably fix this
const val INDICES_INDEX = 2

class Mesh(val nVertices: Int, val nIndices: Int, val nColors: Int) {

    constructor(vertices: FloatArray, indices: IntArray, colors: FloatArray) : this(vertices.size, indices.size, colors.size) {
        writeData(VERTICES_INDEX, vertices)
        writeData(COLORS_INDEX, colors)
        writeIndices(indices)
    }

    val vaoId: Int = glGenVertexArrays()
    private val vboIds = mutableListOf<Int>()
    private val vertexAttribs = hashMapOf<Int, Int>()

    var indicesCount = 0

    init {
        glBindVertexArray(vaoId)

        // order is important: index will be position createBuffer/defineVertexAttrib is called in
        defineVertexAttrib(nVertices, Float.SIZE_BYTES, 3, VERTICES_INDEX)
        defineVertexAttrib(nColors, Float.SIZE_BYTES, 3, COLORS_INDEX)

        createBuffer(nIndices, GL_ELEMENT_ARRAY_BUFFER)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    /**
     * create a buffer and define it as a vertex attribute
     */
    private fun defineVertexAttrib(size: Int, typeSize: Int, attribSize: Int, index: Int) {
        val vboId = createBuffer(size * attribSize, typeSize, GL_ARRAY_BUFFER)
        vertexAttribs[index] = vboId
        glEnableVertexAttribArray(index)
        glVertexAttribPointer(index, attribSize, GL_FLOAT, false, 0, 0)
    }

    /**
     * create a buffer of `size` elements of `typeSize` in bytes with gl type `type`
     */
    private fun createBuffer(size: Int, typeSize: Int, type: Int) : Int {
        return createBuffer(size * typeSize, type)
    }

    /**
     * create a buffer of `size` bytes with an OpenGL type of `type`
     */
    private fun createBuffer(size: Int, type: Int): Int {
        val vboId = glGenBuffers()
        glBindBuffer(type, vboId)
        glBufferData(vboId, size.toLong(), GL_DYNAMIC_DRAW)
        vboIds.add(vboId)
        return vboId
    }

    /**
     * write indices and update indicesCount, assuming the indices array is large enough
     */
    fun writeIndices(data: IntArray) {
        indicesCount = data.size
        glBufferSubData(vboIds[INDICES_INDEX], 0, data)
    }

    /**
     * write `data` to buffer at `index`, assuming the buffer is large enough
     */
    fun writeData(index: Int, data: FloatArray) {
        glBufferSubData(vboIds[index], 0, data)
    }

    /**
     * write `data` to buffer at `index`, assuming the buffer is large enough
     */
    fun writeData(index: Int, data: IntArray) {
        glBufferSubData(vboIds[index], 0, data)
    }

    /**
     * recreate the buffer at `index` with `size` bytes
     */
    private fun recreateBuffer(index: Int, size: Int) {
        glBufferData(vboIds[index], size.toLong(), GL_DYNAMIC_DRAW)
    }

    /**
     * enable all vertex attributes of this mesh
     */
    fun enableVertexAttribs() {
        for (vertexAttrib in vertexAttribs) {
            glEnableVertexAttribArray(vertexAttrib.key)
        }
    }

    /**
     * disable all vertex attributes of this mesh
     */
    fun disableVertexAttribs() {
        for (vertexAttrib in vertexAttribs) {
            glDisableVertexAttribArray(vertexAttrib.key)
        }
    }

    fun discard() {
        glDisableVertexAttribArray(0)

        glBindBuffer(GL_ARRAY_BUFFER, 0)

        for (vboId in vboIds) {
            glDeleteBuffers(vboId)
        }

        glBindVertexArray(0)
        glDeleteVertexArrays(vaoId)
    }
}