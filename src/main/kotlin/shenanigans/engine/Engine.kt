package shenanigans.engine

import com.esotericsoftware.kryonet.Client
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30C.*
import shenanigans.engine.ecs.Resources
import shenanigans.engine.events.Event
import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.StateMachineResource
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.graphics.Renderer
import shenanigans.engine.scene.Scene
import shenanigans.engine.window.Window
import shenanigans.engine.window.WindowResource
import shenanigans.engine.window.events.KeyboardState
import shenanigans.engine.window.events.MouseState

class Engine(initScene: Scene) {

    private lateinit var window: Window

    private var scene: Scene = initScene
    private val resources = Resources()
    private val client = Client()

    private var unprocessedEvents = mutableListOf<Event>();

    fun run() {
        init()
        client.sendTCP("GIMME")
        loop()
        glfwTerminate()
    }

    fun queueEvent(event: Event) {
        unprocessedEvents.add(event)
    }

    private fun init() {
        window = Window("game", 700, 500)

        window.onEvent(::queueEvent)
        resources.set(WindowResource(window))

        resources.set(KeyboardState())
        resources.set(MouseState())
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
            val eventQueue = EventQueue(events.asSequence(), ::queueEvent)
            resources.set(eventQueue)

            resources.resources.forEach { (_, value) ->
                if (value is StateMachineResource) {
                    value.transition(eventQueue)
                }
            }

            val currentTime = glfwGetTime()
            resources.set(DeltaTime(currentTime - previousTime))
            previousTime = currentTime

            scene.runSystems(resources)

            Renderer.renderGame(window, scene)
        }

        Renderer.discard()
    }
}