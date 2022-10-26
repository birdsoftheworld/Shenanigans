package shenanigans.engine.graphics

import org.joml.Vector2f
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
            
            uniform mat4 projectionMatrix;
            uniform mat4 modelViewMatrix;

            void main() {
                gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
                outTexCoord = texCoord;
            }
        """.trimIndent(),
        """
            #version 330

            in vec2 outTexCoord;
            out vec4 fragColor;
            
            uniform sampler2D textureSampler;

            void main() {
                fragColor = texture(textureSampler, outTexCoord);
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
            0f, 1f,
            0f, 0f,
            1f, 0f,
            1f, 1f,
        )
    )

    private val texture = Texture.create("/textureImage.png")

    fun init() {
        shader.createUniform("textureSampler")
        shader.createUniform("projectionMatrix")
        shader.createUniform("modelViewMatrix")
    }

    fun discard() {
        shader.discard()
        mesh.discard()
        texture.discard()
    }

    fun renderGame(window: Window) {
        val width = window.width
        val height = window.height
        glViewport(0, 0, width, height)

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        shader.bind()
        shader.setUniform("projectionMatrix", orthoCamera.getProjectionMatrix(width, height))

        //ecs.runSystem(this) or whatever

        // for testing
        shader.setUniform("modelViewMatrix", orthoCamera.getModelViewMatrix(Vector2f(), 0f, Vector2f(1f, 1f), orthoCamera.getViewMatrix()))
        renderMesh(mesh)

        shader.unbind()

        window.swapBuffers()
    }

    private fun renderMesh(mesh: Mesh) {
        //bind texture
        texture.bind()

        glBindVertexArray(mesh.vaoId)

        mesh.enable()

        glDrawElements(GL_TRIANGLES, mesh.indicesCount, GL_UNSIGNED_INT, 0)

        mesh.disable()

        texture.unbind()

        glBindVertexArray(0)
    }
}