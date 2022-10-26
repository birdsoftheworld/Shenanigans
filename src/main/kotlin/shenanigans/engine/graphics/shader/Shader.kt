package shenanigans.engine.graphics.shader

import org.joml.Matrix4f
import org.lwjgl.opengl.GL30C.*
import org.lwjgl.system.MemoryStack
import java.io.File

class Shader(vertexShader: String, fragmentShader: String) {
    private val programId: Int = glCreateProgram()
    private val vertShaderId: Int
    private val fragShaderId: Int

    private val uniforms = hashMapOf<String, Int>()

    init {
        vertShaderId = createShader(vertexShader, GL_VERTEX_SHADER)
        fragShaderId = createShader(fragmentShader, GL_FRAGMENT_SHADER)

        glLinkProgram(programId)

        if(glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw RuntimeException("Failed to link program: ${glGetProgramInfoLog(programId, 1024)}")
        }

        if(vertShaderId != 0) {
            glDetachShader(programId, vertShaderId)
        }
        if(fragShaderId != 0) {
            glDetachShader(programId, fragShaderId)
        }

        // only necessary for debugging
        glValidateProgram(programId)
        if(glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Shader validation warning: ${glGetProgramInfoLog(programId, 1024)}")
        }
    }

    private fun createShader(source: String, type: Int): Int {
        val id = glCreateShader(type)

        if(id == 0) {
            throw RuntimeException("Failed to create shader of type $type")
        }

        glShaderSource(id, source)
        glCompileShader(id)

        if(glGetShaderi(id, GL_COMPILE_STATUS) == 0) {
            throw IllegalStateException("Failed to compile shader: ${glGetShaderInfoLog(id, 1024)}")
        }

        glAttachShader(programId, id)

        return id
    }

    fun bind() {
        glUseProgram(programId)
    }

    fun unbind() {
        glUseProgram(0)
    }

    fun discard() {
        unbind()
        glDeleteProgram(programId)
    }

    fun createUniform(name: String) {
        val uniform = glGetUniformLocation(programId, name)
        if(uniform < 0) {
            throw IllegalStateException("Failed to find uniform location: $name")
        }
        uniforms[name] = uniform
    }

    fun setUniform(name: String, value: Matrix4f) {
        val stack = MemoryStack.stackPush()
        stack.use {
            val buffer = it.mallocFloat(16)
            value.get(buffer) // put matrix into buffer
            glUniformMatrix4fv(uniforms[name]!!, false, buffer)
        }
    }

    fun setUniform(name : String, value : Int){
        glUniform1i(uniforms[name]!!, value)
    }

    companion object {
        fun create(vertexFile: File, fragmentFile: File): Shader {
            val vertex = read(vertexFile)
            val fragment = read(fragmentFile)

            return Shader(vertex, fragment)
        }

        private fun read(file: File): String {
            val bufferedReader = file.bufferedReader()
            return bufferedReader.use { it.readText() }
        }
    }
}