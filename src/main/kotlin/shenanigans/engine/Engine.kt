package shenanigans.engine

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30C.*
import shenanigans.engine.api.SceneManager
import shenanigans.engine.resources.DeltaTime
import shenanigans.engine.graphics.Renderer
import shenanigans.engine.window.Window
import shenanigans.engine.scene.Scene

class Engine {

    private lateinit var window: Window

    private val sceneManager = SceneManager();

    private fun init() {
        window = Window("game", 640, 480)
    }

    fun run() {
        init()
        loop()
        glfwTerminate()
    }

    private fun loop() {
        GL.createCapabilities()
        Renderer.init()

        glClearColor(0.5f, 1.0f, 0.5f, 0.5f)
        var previousTime = glfwGetTime()

        while (!window.shouldClose) {
            val currentTime = glfwGetTime()
            val deltaTime = DeltaTime(currentTime - previousTime)

            glfwPollEvents()
            //Events.loadEvents()

            sceneManager.scene.runSystems(deltaTime)

            Renderer.renderGame(window)

            previousTime = currentTime
        }

        Renderer.discard()
    }
}