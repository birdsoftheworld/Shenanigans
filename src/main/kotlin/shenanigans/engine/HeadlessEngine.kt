package shenanigans.engine

import org.lwjgl.glfw.GLFW
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.ecs.System
import shenanigans.engine.events.engine.InitializationEvent
import shenanigans.engine.net.Network
import shenanigans.engine.net.Server
import shenanigans.engine.physics.Time
import shenanigans.engine.scene.Scene
import shenanigans.engine.timer.TimerSystem
import kotlin.system.exitProcess

class HeadlessEngine(initScene: Scene, network: Network = Network(Server())) : Engine(initScene, network) {
    override fun init() {
    }

    override fun loop() {
        var lastTick = 0L

        network.impl.connect()

        physicsEvents.queueLater(InitializationEvent)
        physicsEvents.finish()

        var previousTime = GLFW.glfwGetTime()

        while (true) {
            lastTick = java.lang.System.currentTimeMillis()
            handleControlEvents(physicsEvents)
            handleControlEvents(networkEvents)

            transitionStateMachineResources(eventQueuesFor(physicsEvents)) // FIXME: shouldn't be physics events here

            val currentTime = GLFW.glfwGetTime()
            engineResources.set(Time(currentTime - previousTime, currentTime))
            previousTime = currentTime

            val physicsResources = ResourcesView(scene.sceneResources, engineResources)
            scene.defaultSystems.asSequence().plus(builtinSystems).forEach(
                scene.runSystem(
                    System::executePhysics,
                    physicsResources,
                    eventQueuesFor(physicsEvents)
                )
            )

            val networkResources = ResourcesView(scene.sceneResources, engineResources)
            scene.defaultSystems.asSequence().plus(TimerSystem).forEach(
                scene.runSystem(
                    System::executeNetwork,
                    networkResources,
                    eventQueuesFor(networkEvents)
                )
            )

            physicsEvents.finish()
            networkEvents.finish()
            renderEvents.finish()

            while (java.lang.System.currentTimeMillis() - lastTick < 20) {
                continue
            }
        }
    }

    override fun exit() {
        GLFW.glfwTerminate()

        exitProcess(0)
    }
}