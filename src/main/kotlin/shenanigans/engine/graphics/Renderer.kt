package shenanigans.engine.graphics

import org.lwjgl.opengl.GL30C.*
import shenanigans.engine.graphics.shader.Shader
import shenanigans.engine.util.OrthoCamera
import shenanigans.engine.window.Window

object Renderer {
    private val orthoCamera = OrthoCamera()

    private val shader = Shader(
        """
            #version 330

            layout (location=0) in vec3 position;
            layout (location=1) in vec3 inColor;

            out vec3 outColor;
            
            uniform mat4 projectionMatrix;

            void main() {
                gl_Position = projectionMatrix * vec4(position, 1.0);
                outColor = inColor;
            }
        """.trimIndent(),
        """
            #version 330

            in vec3 outColor;
            out vec4 fragColor;

            void main() {
                fragColor = vec4(outColor, 1.0);
            }
        """.trimIndent(),
    )
    private val mesh = Mesh(
        floatArrayOf(
            0f, 100f, 0f,
            0f, 0f, 0f,
            100f, 0f, 0f,
            100f, 100f, 0f,
        ),
        intArrayOf(
            0, 1, 3, 3, 1, 2,
        ),
        floatArrayOf(
            0.5f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f,
            0.0f, 0.0f, 0.5f,
            0.0f, 0.5f, 0.5f,
        ),
    )

    fun init() {
        shader.createUniform("projectionMatrix")
        shader.createUniform("worldMatrix")
    }

    fun discard() {
        shader.discard()
        mesh.discard()
    }

    fun renderGame(window: Window) {
        val width = window.width
        val height = window.height
        glViewport(0, 0, width, height)

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        shader.bind()
        shader.setUniform("projectionMatrix", orthoCamera.getProjectionMatrix(width, height))

        renderMesh(mesh)

        shader.unbind()

        window.swapBuffers()
    }

    private fun renderMesh(mesh: Mesh) {
        glBindVertexArray(mesh.vaoId)

        mesh.enableVertexAttribs()
        glDrawElements(GL_TRIANGLES, mesh.verticesCount, GL_UNSIGNED_INT, 0)
        mesh.disableVertexAttribs()

        glBindVertexArray(0)
    }
}