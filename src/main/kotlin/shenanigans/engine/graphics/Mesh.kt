package shenanigans.engine.graphics

import org.lwjgl.opengl.GL30C.*
import java.lang.IllegalArgumentException

private const val INDEX = "index"

class Mesh(nVertices: Int, nIndices: Int, vertexAttribs: Set<VertexAttribute>) {
    constructor(vertices: FloatArray, indices: IntArray, texCoords: FloatArray) : this(
        vertices.size / 3,
        indices.size,
        setOf(VertexAttribute.POSITION, VertexAttribute.TEX_COORDS)
    ) {
        writeIndices(indices)
        writeData(VertexAttribute.POSITION, vertices)
        writeData(VertexAttribute.TEX_COORDS, texCoords)
    }

    val vaoId: Int = glGenVertexArrays()
    private val vboIds = hashMapOf<String, Int>()
    private val vertexAttribs = hashMapOf<Int, Int>()

    var indicesCount = 0

    init {
        if(!vertexAttribs.contains(VertexAttribute.POSITION)) {
            throw IllegalArgumentException("Must include position in vertex attributes")
        }

        glBindVertexArray(vaoId)

        createBuffer(nIndices, Int.SIZE_BYTES, GL_ELEMENT_ARRAY_BUFFER, INDEX)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        for (vertexAttrib in vertexAttribs) {
            defineVertexAttrib(nVertices, vertexAttrib.typeSize, vertexAttrib.attributeSize, vertexAttrib.index, vertexAttrib.name)
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    /**
     * create a buffer and define it as a vertex attribute
     */
    private fun defineVertexAttrib(size: Int, typeSize: Int, attribSize: Int, index: Int, name: String) {
        val vboId = createBuffer(size * attribSize, typeSize, GL_ARRAY_BUFFER, name)
        vertexAttribs[index] = vboId
        glEnableVertexAttribArray(index)
        glVertexAttribPointer(index, attribSize, GL_FLOAT, false, 0, 0)
    }

    /**
     * create a buffer of `size` elements of `typeSize` in bytes with gl type `type`
     */
    private fun createBuffer(size: Int, typeSize: Int, type: Int, name: String) : Int {
        return createBuffer(size * typeSize, type, name)
    }

    /**
     * create a buffer of `size` bytes with an OpenGL type of `type`
     */
    private fun createBuffer(size: Int, type: Int, name: String): Int {
        val vboId = glGenBuffers()
        glBindBuffer(type, vboId)
        glBufferData(type, size.toLong(), GL_DYNAMIC_DRAW)
        vboIds[name] = vboId
        // buffer is not unbound, because it needs to be bound later in defineVertexAttrib
        return vboId
    }

    /**
     * write indices and update indicesCount, assuming the indices buffer is large enough
     */
    fun writeIndices(data: IntArray) {
        indicesCount = data.size
        writeData(INDEX, data, GL_ELEMENT_ARRAY_BUFFER)
    }

    /**
     * write `data` to vertex attribute's buffer, assuming the buffer is large enough
     */
    fun writeData(attrib: VertexAttribute, data: FloatArray) {
        writeData(attrib.name, data, GL_ARRAY_BUFFER)
    }

    /**
     * write `data` to buffer `name`, assuming the buffer is large enough
     */
    private fun writeData(name: String, data: FloatArray, type: Int) {
        glBindBuffer(type, vboIds[name]!!)
        glBufferSubData(type, 0, data)
        glBindBuffer(type, 0)
    }

    /**
     * write `data` to vertex attribute's buffer, assuming the buffer is large enough
     */
    fun writeData(attrib: VertexAttribute, data: IntArray) {
        writeData(attrib.name, data, GL_ARRAY_BUFFER)
    }

    /**
     * write `data` to buffer `name`, assuming the buffer is large enough
     */
    private fun writeData(name: String, data: IntArray, type: Int) {
        glBindBuffer(type, vboIds[name]!!)
        glBufferSubData(type, 0, data)
        glBindBuffer(type, 0)
    }

    /**
     * enable vertex attributes and indices of this mesh
     */
    fun enable() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIds[INDEX]!!)
        for ((index, _) in vertexAttribs) {
            glEnableVertexAttribArray(index)
        }
    }

    /**
     * disable vertex attributes and indices of this mesh
     */
    fun disable() {
        for ((index, _) in vertexAttribs) {
            glDisableVertexAttribArray(index)
        }
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun discard() {
        glDisableVertexAttribArray(0)

        glBindBuffer(GL_ARRAY_BUFFER, 0)

        for ((_, vboId) in vboIds) {
            glDeleteBuffers(vboId)
        }

        glBindVertexArray(0)

        glDeleteVertexArrays(vaoId)
    }

    fun render() {
        glBindVertexArray(vaoId)
        enable()
        glDrawElements(GL_TRIANGLES, indicesCount, GL_UNSIGNED_INT, 0)
        disable()
        glBindVertexArray(0)
    }
}