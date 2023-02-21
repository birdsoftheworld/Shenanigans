package shenanigans.engine

import org.lwjgl.glfw.GLFW
import shenanigans.engine.ecs.EntitiesLifecycle
import shenanigans.engine.ecs.EntitiesView
import shenanigans.engine.ecs.Resources
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.ecs.System
import shenanigans.engine.events.*
import shenanigans.engine.events.control.ControlEvent
import shenanigans.engine.events.control.ExitEvent
import shenanigans.engine.events.control.SceneChangeEvent
import shenanigans.engine.events.control.UpdateDefaultSystemsEvent
import shenanigans.engine.net.Network
import shenanigans.engine.net.Server
import shenanigans.engine.scene.Scene

abstract class Engine(initScene: Scene) {
    protected var scene: Scene = initScene
    protected val engineResources = Resources()

    val network = Network(Server())

    val physicsEvents = LocalEventQueue()
    val renderEvents = LocalEventQueue()
    val networkEvents = network.createEventQueue()

    fun run() {
        if (!GLFW.glfwInit()) throw RuntimeException("Failed to initialize GLFW")
        init()
        loop()
    }

    abstract fun init()

    abstract fun loop()

    abstract fun exit()

    fun runPhysicsOnce(system: System) {
        scene.runSystem(
            System::executePhysics,
            ResourcesView(engineResources, scene.sceneResources),
            eventQueuesFor(physicsEvents)
        )
    }

    protected fun handleControlEvents(events: EventQueue) {
        events.receive(ControlEvent::class).forEach { e ->
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

    protected fun <Q : EventQueue?> eventQueuesFor(own: Q): EventQueues<Q> {
        return eventQueues(
            physics = physicsEvents,
            network = networkEvents,
            render = renderEvents,
            own = own
        )
    }
}