package shenanigans.engine.graphics

import org.lwjgl.opengl.GL30C.*
import org.lwjgl.system.MemoryUtil

class Mesh(private val vertices: FloatArray, private val indices: IntArray, private val colors: FloatArray) {
    val vaoId: Int = glGenVertexArrays()
    private val vboIds = mutableListOf<Int>()
    private val vertexAttribs = hashMapOf<Int, Int>()

    val verticesCount
        get() = vertices.size

    init {
        glBindVertexArray(vaoId)

        defineBufferData(indices, GL_ELEMENT_ARRAY_BUFFER)

        defineVertexAttrib(vertices, 3, 0)
        defineVertexAttrib(colors, 3, 1)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    private fun defineVertexAttrib(array: FloatArray, size: Int, index: Int) {
        val vboId = defineBufferData(array, GL_ARRAY_BUFFER)
        vertexAttribs[index] = vboId
        glEnableVertexAttribArray(index)
        glVertexAttribPointer(index, size, GL_FLOAT, false, 0, 0)
    }

    private fun defineBufferData(array: FloatArray, type: Int): Int {
        val buffer = MemoryUtil.memAllocFloat(array.size)
        buffer.put(array).flip()
        val vboId = glGenBuffers()
        glBindBuffer(type, vboId)
        glBufferData(type, buffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(buffer)
        vboIds.add(vboId)
        return vboId
    }

    private fun defineBufferData(array: IntArray, type: Int): Int {
        val buffer = MemoryUtil.memAllocInt(array.size)
        buffer.put(array).flip()
        val vboId = glGenBuffers()
        glBindBuffer(type, vboId)
        glBufferData(type, buffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(buffer)
        vboIds.add(vboId)
        return vboId
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