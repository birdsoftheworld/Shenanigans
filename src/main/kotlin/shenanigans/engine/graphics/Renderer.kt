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
            layout (location=1) in vec2 texCoord;

            out vec2 outTexCoord;
            
            uniform mat4 worldMatrix;
            uniform mat4 projectionMatrix;

            void main() {
                gl_Position = projectionMatrix * worldMatrix * vec4(position, 1.0);
                outTexCoord = texCoord;
            }
        """.trimIndent(),
        """
            #version 330

            in vec2 outTexCoord;
            out vec4 fragColor;
            
            uniform sampler2D texture_sampler;

            void main() {
                fragColor = texture(texture_sampler, outTexCoord);
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

        floatArrayOf(
            0f, 1f,
            0f, 0f,
            1f, 0f,
            1f, 1f,
            ),

        intArrayOf(
            0, 1, 3, 3, 1, 2,
        ),
        texture = Texture(),
    )

    fun init() {
        shader.createUniform("texture_sampler")
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
        shader.setUniform("texture_sampler", orthoCamera.getProjectionMatrix(width, height))

        renderMesh(mesh)

        shader.unbind()

        window.swapBuffers()
    }

    private fun renderMesh(mesh: Mesh) {
        //activate texture unit
        glActiveTexture(GL_TEXTURE0)

        //bind texture
        glBindTexture(GL_TEXTURE_2D, mesh.texture.textureId)

        glBindVertexArray(mesh.vboId)

        mesh.enableVertexAttribs()
        glDrawElements(GL_TRIANGLES, mesh.verticesCount, GL_UNSIGNED_INT, 0)
        mesh.disableVertexAttribs()

        glBindVertexArray(0)
    }
}