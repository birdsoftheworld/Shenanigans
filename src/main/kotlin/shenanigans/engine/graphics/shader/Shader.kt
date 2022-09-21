package shenanigans.engine.graphics.shader

import org.lwjgl.opengl.GL30C.*

class Shader(vertexShader: String, fragmentShader: String) {
    private val programId: Int

    init {
        programId = glCreateProgram()

        val vertShaderId = glCreateShader(GL_VERTEX_SHADER)
        val fragShaderId = glCreateShader(GL_FRAGMENT_SHADER)

        glShaderSource(vertShaderId, vertexShader)
        glShaderSource(fragShaderId, fragmentShader)

        glAttachShader(programId, vertShaderId)
        glAttachShader(programId, fragShaderId)

        glDeleteShader(vertShaderId)
        glDeleteShader(fragShaderId)
    }

    fun use() {
        glUseProgram(programId)
    }

    fun discard() {
        glDeleteProgram(programId)
    }
}