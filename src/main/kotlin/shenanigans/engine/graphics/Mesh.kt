package shenanigans.engine.graphics

import org.lwjgl.opengl.GL30C.*
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer


class Mesh {
    private val vaoId: Int = glGenVertexArrays()
    private val vboId: Int

    init {
        glBindVertexArray(vaoId)

        val verticesBuffer: FloatBuffer = MemoryUtil.memAllocFloat(0)
        verticesBuffer
//            .put(vertices)
            .flip()

        vboId = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vboId)
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)

        MemoryUtil.memFree(verticesBuffer)

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    fun doThings() {
        glBindVertexArray(vaoId)
        glEnableVertexAttribArray(0)

        glDrawArrays(GL_TRIANGLES, 0, 3)

        glDisableVertexAttribArray(0)
        glBindVertexArray(0)
    }

    fun discard() {
        glDisableVertexAttribArray(0)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glDeleteBuffers(vboId)

        glBindVertexArray(0)
        glDeleteVertexArrays(vaoId)
    }
}