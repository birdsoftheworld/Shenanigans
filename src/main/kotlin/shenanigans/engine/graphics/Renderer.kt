package shenanigans.engine.graphics

import org.lwjgl.opengl.GL30C.*
import org.lwjgl.opengl.GLUtil
import shenanigans.engine.ecs.Resources
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.scene.Scene
import shenanigans.engine.util.OrthoCamera
import shenanigans.engine.window.Window

object Renderer {
    private val orthoCamera = OrthoCamera()

    private val shapeSystem = ShapeSystem()
    private val spriteSystem = SpriteSystem()

    private val renderSystems = listOf(shapeSystem, spriteSystem)

    fun init() {
        if(System.getProperty("render_debug") != null) {
            GLUtil.setupDebugMessageCallback()
        }
        TextureManager.initialize()
        glEnable(GL_MULTISAMPLE)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    fun discard() {
        TextureManager.discard()
    }

    fun renderGame(window: Window, scene: Scene) {
        val width = window.width
        val height = window.height
        glViewport(0, 0, width, height)
        orthoCamera.setScreenSize(width, height)

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        val resources = Resources()
        resources.set(CameraResource(orthoCamera))
        scene.runSystems(resources, renderSystems)

        window.swapBuffers()
    }
}