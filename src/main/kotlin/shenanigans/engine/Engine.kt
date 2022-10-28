package shenanigans.engine

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30C.*
import shenanigans.engine.resources.DeltaTime
import shenanigans.engine.graphics.Renderer
import shenanigans.engine.window.Window
import shenanigans.engine.scene.Scene

class Engine {

    private lateinit var window: Window

    private lateinit var scene: Scene

    private fun init() {
        window = Window("game", 640, 480)

        scene = Scene()
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

            scene.setResource(deltaTime)
            scene.runSystems()

            Renderer.renderGame(window, scene)

            previousTime = currentTime
        }

        Renderer.discard()
    }
}