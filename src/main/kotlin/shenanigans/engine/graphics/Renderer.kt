package shenanigans.engine.graphics

import org.joml.Matrix4f
import org.lwjgl.opengl.GL30C.*
import shenanigans.engine.graphics.shader.Shader
import shenanigans.engine.window.Window

object Renderer {
    private var previousWidth = -1
    private var previousHeight = -1
    private val projectionMatrix: Matrix4f = Matrix4f()

    private val FOV = Math.toRadians(60.0).toFloat()
    private const val Z_NEAR = 0.01f
    private const val Z_FAR = 1000f

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
            -0.5f, 0.5f, -1.05f,
            -0.5f, -0.5f, -1.05f,
            0.5f, -0.5f, -1.05f,
            0.5f, 0.5f, -1.05f,
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
    }

    fun discard() {
        shader.discard()
        mesh.discard()
    }

    fun renderGame(window: Window) {
        val width = window.width
        val height = window.height
        glViewport(0, 0, width, height)

        if(width != previousWidth || height != previousHeight) {
            previousWidth = width
            previousHeight = height

            val aspectRatio: Float = width / height.toFloat()
            projectionMatrix.setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR)
        }

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        shader.bind()
        shader.setUniform("projectionMatrix", projectionMatrix)

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