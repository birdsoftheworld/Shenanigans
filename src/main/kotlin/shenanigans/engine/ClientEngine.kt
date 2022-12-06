package shenanigans.engine

import com.esotericsoftware.kryonet.Client
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30C
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

class ClientEngine (initScene: Scene) : Engine(initScene = initScene) {
    private lateinit var window: Window

    private val client = Client()

    override fun init() {
        window = Window("game", 640, 640)

        window.onEvent(::queueEvent)
        resources.set(WindowResource(window))

        resources.set(KeyboardState())
        resources.set(MouseState())
    }

    override fun loop() {
        GL.createCapabilities()
        Renderer.init()

        GL30C.glClearColor(0.5f, 1.0f, 0.5f, 0.5f)
        var previousTime = GLFW.glfwGetTime()

        while (!window.shouldClose) {
            GLFW.glfwPollEvents()

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

            resources.set(eventQueue)

            resources.resources.forEach { (_, value) ->
                if (value is StateMachineResource) {
                    value.transition(eventQueue)
                }
            }

            val currentTime = GLFW.glfwGetTime()
            resources.set(DeltaTime(currentTime - previousTime))
            previousTime = currentTime

            scene.runSystems(resources)

            Renderer.renderGame(window, scene)
        }

        Renderer.discard()
    }
}

annotation class ClientOnly