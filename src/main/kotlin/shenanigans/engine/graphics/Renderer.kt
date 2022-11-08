package shenanigans.engine.graphics

import org.lwjgl.opengl.GL30C.*
import org.lwjgl.opengl.GLUtil
import shenanigans.engine.ecs.Resources
import shenanigans.engine.graphics.api.ShapeRenderer
import shenanigans.engine.graphics.shader.Shader
import shenanigans.engine.resources.CameraResource
import shenanigans.engine.scene.Scene
import shenanigans.engine.util.OrthoCamera
import shenanigans.engine.window.Window

object Renderer {
    private val orthoCamera = OrthoCamera()

    private val shapeSystem = ShapeSystem()
    private val textureSystem = TextureSystem()

    fun init() {
        GLUtil.setupDebugMessageCallback()
    }

    fun discard() {
    }

    fun renderGame(window: Window, scene: Scene) {
        val width = window.width
        val height = window.height
        glViewport(0, 0, width, height)
        orthoCamera.setScreenSize(width, height)

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

//        shader.bind()
//        shader.setUniform("projectionMatrix", orthoCamera.getProjectionMatrix())
//
//        // for testing
//        shader.setUniform("modelViewMatrix", orthoCamera.getModelViewMatrix(Vector2f(), 0f, Vector2f(1f, 1f), orthoCamera.getViewMatrix()))
//        renderMesh(mesh)
//
//        shader.unbind()

        val resources = Resources()
        resources.set(CameraResource(orthoCamera))
        //scene.runSystems(resources, listOf(shapeSystem))
        scene.runSystems(resources, listOf(textureSystem))

        window.swapBuffers()
    }

    private fun renderMesh(mesh: Mesh) {
        //bind texture
        texture.bind()

        mesh.render()

        texture.unbind()
    }
}