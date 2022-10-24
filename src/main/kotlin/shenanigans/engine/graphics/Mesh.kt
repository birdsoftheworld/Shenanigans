package shenanigans.engine.graphics

import org.lwjgl.opengl.GL15C
import org.lwjgl.opengl.GL20C
import org.lwjgl.opengl.GL30C.*
import org.lwjgl.system.MemoryUtil

class Mesh(private val vertices: FloatArray, private val textCoords: FloatArray, private val indices: IntArray, val texture: Texture) {
    val vboId: Int = glGenBuffers()
    private val vboIds = mutableListOf<Int>()
    private val vertexAttribs = hashMapOf<Int, Int>()

    val verticesCount
        get() = vertices.size

    init {
        vboIds.add(vboId)
        val textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.size);
        textCoordsBuffer.put(textCoords).flip()

        //defineBufferData(indices, GL_ELEMENT_ARRAY_BUFFER)

        defineVertexAttrib(vertices, 3, 0)
        defineVertexAttrib(textCoords, 2, 1)

        glBindBuffer(GL_ARRAY_BUFFER, vboId)
        glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW)

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)

        //glBindVertexArray(0)
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
        glDeleteVertexArrays(vboId)
    }
}