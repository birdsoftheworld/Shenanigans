package shenanigans.engine

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30C.*
import shenanigans.engine.ecs.Resources
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.graphics.Renderer
import shenanigans.engine.scene.Scene
import shenanigans.engine.window.KeyboardInput
import shenanigans.engine.window.Window
import shenanigans.engine.window.WindowResource

class Engine {

    private lateinit var window: Window

    private val scene = Scene()
    private val resources = Resources()

    fun run() {
        init()
        loop()
        glfwTerminate()
    }

    private fun init() {
        window = Window("game", 640, 480)
        resources.set(WindowResource(window))
        resources.set(KeyboardInput(window))
    }

    private fun loop() {
        GL.createCapabilities()
        Renderer.init()

        glClearColor(0.5f, 1.0f, 0.5f, 0.5f)
        var previousTime = glfwGetTime()

        while (!window.shouldClose) {
            val currentTime = glfwGetTime()
            resources.set(DeltaTime(currentTime - previousTime))
            previousTime = currentTime

            glfwPollEvents()

            scene.runSystems(resources)

            Renderer.renderGame(window, scene)
        }

        Renderer.discard()
    }
}