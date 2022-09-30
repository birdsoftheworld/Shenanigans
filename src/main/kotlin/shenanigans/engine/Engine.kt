package shenanigans.engine

import shenanigans.engine.window.Window
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30C.*
import shenanigans.engine.graphics.Renderer

class Engine {

    private lateinit var window: Window

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

        glClearColor(0.5f, 1.0f, 0.5f, 0.5f)
        var previousTime = glfwGetTime();

        while (!window.shouldClose) {
            val currentTime = glfwGetTime()
            val deltaTime = currentTime - previousTime

            glfwPollEvents()
            // Events.loadEvents()

            // Entities.runSystems(deltaTime)

            Renderer.renderGame(window)

            previousTime = currentTime
        }

        Renderer.discard()
    }
}