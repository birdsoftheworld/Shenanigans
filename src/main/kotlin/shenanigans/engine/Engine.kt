package shenanigans.engine

import com.esotericsoftware.kryonet.Client
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30C.glClearColor
import shenanigans.engine.ecs.Resources
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.events.Event
import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.StateMachineResource
import shenanigans.engine.events.control.ControlEvent
import shenanigans.engine.events.control.ExitEvent
import shenanigans.engine.events.control.SceneChangeEvent
import shenanigans.engine.events.control.UpdateDefaultSystemsEvent
import shenanigans.engine.graphics.Renderer
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.scene.Scene
import shenanigans.engine.window.Window
import shenanigans.engine.window.WindowResource
import shenanigans.engine.window.events.KeyboardState
import shenanigans.engine.window.events.MouseState

class Engine(initScene: Scene) {
    private lateinit var window: Window

    internal val engineResources = Resources()
    private var scene: Scene = initScene
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
        window = Window("game", 640, 640)

        window.onEvent(::queueEvent)
        engineResources.set(WindowResource(window))

        engineResources.set(KeyboardState())
        engineResources.set(MouseState())
    }

    private fun loop() {
        GL.createCapabilities()
        Renderer.init(this)

        glClearColor(0.5f, 1.0f, 0.5f, 0.5f)
        var previousTime = glfwGetTime()

        while (!window.shouldClose) {
            glfwPollEvents()

            // shhhhh just pretend this is atomic
            val events = unprocessedEvents
            unprocessedEvents = mutableListOf()
            val eventQueue = EventQueue(events, ::queueEvent)

            val exit = eventQueue.iterate<ControlEvent>().any { e ->
                when (e) {
                    is ExitEvent -> true
                    is SceneChangeEvent -> {
                        scene = e.scene
                        false
                    }
                    is UpdateDefaultSystemsEvent -> {
                        e.update(scene.defaultSystems)
                        false
                    }
                }
            }
            if (exit) {
                break
            }

            engineResources.set(eventQueue)

            engineResources.resources.forEach { (_, value) ->
                if (value is StateMachineResource) {
                    value.transition(eventQueue)
                }
            }

            val currentTime = glfwGetTime()
            engineResources.set(DeltaTime(currentTime - previousTime))
            previousTime = currentTime

            scene.runSystems(ResourcesView(scene.sceneResources, engineResources))

            Renderer.renderGame(window, scene, engineResources)
        }

        Renderer.discard()
    }
}