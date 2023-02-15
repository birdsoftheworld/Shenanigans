package shenanigans.engine

import org.lwjgl.glfw.GLFW
import shenanigans.engine.ecs.Resources
import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.events.StateMachine
import shenanigans.engine.events.control.ControlEvent
import shenanigans.engine.events.control.ExitEvent
import shenanigans.engine.events.control.SceneChangeEvent
import shenanigans.engine.events.control.UpdateDefaultSystemsEvent
import shenanigans.engine.net.Network
import shenanigans.engine.scene.Scene

abstract class Engine(initScene: Scene) {
    protected var scene: Scene = initScene
    protected var network: Network? = null
    protected val engineResources = Resources()

    val physicsEvents: EventQueue = LocalEventQueue()
    val renderEvents: EventQueue = LocalEventQueue()
    val networkEvents: EventQueue = LocalEventQueue()

    fun run() {
        if (!GLFW.glfwInit()) throw RuntimeException("Failed to initialize GLFW")
        init()
        loop()
    }

    abstract fun init()

    abstract fun loop()

    abstract fun exit()

    protected fun handleControlEvents(events: EventQueue) {
        events.iterate<ControlEvent>().forEach { e ->
            when (e) {
                is ExitEvent -> {
                    exit()
                }

                is SceneChangeEvent -> {
                    scene = e.scene
                }

                is UpdateDefaultSystemsEvent -> {
                    e.update(scene.defaultSystems)
                }
            }
        }
    }

    protected fun transitionStateMachineResources(events: EventQueue) {
        engineResources.resources.forEach { (_, value) ->
            if (value is StateMachine) {
                value.transition(events)
            }
        }
    }

    protected fun eventQueuesFor(own: EventQueue): EventQueues {
        return EventQueues(
            physics = physicsEvents,
            network = networkEvents,
            render = renderEvents,
            own = own
        )
    }
}