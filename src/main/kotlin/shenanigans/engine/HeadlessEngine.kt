package shenanigans.engine

import org.lwjgl.glfw.GLFW
import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.StateMachineResource
import shenanigans.engine.events.control.ControlEvent
import shenanigans.engine.events.control.ExitEvent
import shenanigans.engine.events.control.SceneChangeEvent
import shenanigans.engine.events.control.UpdateDefaultSystemsEvent
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.scene.Scene

class HeadlessEngine(initScene: Scene) : Engine(initScene){
    var running = true

    override fun init() {

    }

    override fun loop() {

        var previousTime = GLFW.glfwGetTime()

        while (running) {
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
        }
    }

}