package shenanigans.engine

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30C.*
import shenanigans.engine.ecs.Resources
import shenanigans.engine.events.Event
import shenanigans.engine.events.EventQueue
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.graphics.Renderer
import shenanigans.engine.scene.Scene
import shenanigans.engine.window.Window
import shenanigans.engine.window.WindowResource

class Engine {

    private lateinit var window: Window

    private var scene = Scene()
    private val resources = Resources()

    private var unprocessedEvents = mutableListOf<Event>();

    fun run() {
        init()
        loop()
        glfwTerminate()
    }

    fun queueEvent(event: Event) {
        unprocessedEvents.add(event)
    }

    private fun init() {
        window = Window("game", 640, 480)
        window.onEvent(::queueEvent)
        resources.set(WindowResource(window))
    }

    private fun loop() {
        GL.createCapabilities()
        Renderer.init()

        glClearColor(0.5f, 1.0f, 0.5f, 0.5f)
        var previousTime = glfwGetTime()

        while (!window.shouldClose) {
            glfwPollEvents()

            // shhhhh just pretend this is atomic
            val events = unprocessedEvents
            unprocessedEvents = mutableListOf()
            resources.set(EventQueue(events.asSequence()))

            val currentTime = glfwGetTime()
            resources.set(DeltaTime(currentTime - previousTime))
            previousTime = currentTime

            scene.runSystems(resources)

            Renderer.renderGame(window, scene)
        }

        Renderer.discard()
    }
}