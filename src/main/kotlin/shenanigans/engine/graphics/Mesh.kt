package shenanigans.engine.graphics

import org.lwjgl.opengl.GL30C.*
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer


class Mesh(private val vertices: FloatArray, private val indices: IntArray, private val colors: FloatArray) {
    val vaoId: Int = glGenVertexArrays()
    private val posVboId: Int
    private val idxVboId: Int
    private val colVboId: Int

    val verticesCount
        get() = vertices.size

    init {
        glBindVertexArray(vaoId)

        val indicesBuffer = MemoryUtil.memAllocInt(indices.size)
        indicesBuffer
            .put(indices)
            .flip()
        idxVboId = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(indicesBuffer)

        val verticesBuffer: FloatBuffer = MemoryUtil.memAllocFloat(verticesCount)
        verticesBuffer
            .put(vertices)
            .flip()
        posVboId = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, posVboId)
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)
        MemoryUtil.memFree(verticesBuffer)

        val colorBuffer = MemoryUtil.memAllocFloat(colors.size)
        colorBuffer
            .put(colors)
            .flip()
        colVboId = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, colVboId)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, colorBuffer, GL_STATIC_DRAW)
        glEnableVertexAttribArray(1)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0)
        MemoryUtil.memFree(colorBuffer)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    fun discard() {
        glDisableVertexAttribArray(0)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glDeleteBuffers(posVboId)
        glDeleteBuffers(idxVboId)
        glDeleteBuffers(colVboId)

        glBindVertexArray(0)
        glDeleteVertexArrays(vaoId)
    }
}