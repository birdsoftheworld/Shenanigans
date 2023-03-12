package shenanigans.engine

import org.lwjgl.glfw.GLFW
import shenanigans.engine.ecs.Resources
import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.StateMachine
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.ecs.System
import shenanigans.engine.events.*
import shenanigans.engine.events.control.ControlEvent
import shenanigans.engine.events.control.ExitEvent
import shenanigans.engine.events.control.SceneChangeEvent
import shenanigans.engine.events.control.UpdateDefaultSystemsEvent
import shenanigans.engine.net.Network
import shenanigans.engine.scene.Scene

abstract class Engine(initScene: Scene, val network: Network) {
    protected var scene: Scene = initScene
    protected val engineResources = Resources()

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
        )(system)
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

    protected fun transitionStateMachineResources(events: EventQueues<LocalEventQueue>) {
        engineResources.resources.forEach { (_, value) ->
            if (value is StateMachine) {
                value.transitionPhysics(events.physics)
                value.transitionRender(events.render)
                value.transitionNetwork(events.network)
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