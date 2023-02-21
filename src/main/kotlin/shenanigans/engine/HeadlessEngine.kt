package shenanigans.engine

import org.lwjgl.glfw.GLFW
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.ecs.System
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.scene.Scene
import kotlin.system.exitProcess

class HeadlessEngine(initScene: Scene) : Engine(initScene) {
    override fun init() {
    }

    override fun loop() {
        var previousTime = GLFW.glfwGetTime()

        while (true) {
            handleControlEvents(physicsEvents)
            handleControlEvents(networkEvents)

            transitionStateMachineResources(eventQueuesFor(physicsEvents)) // FIXME: shouldn't be physics events here

            val currentTime = GLFW.glfwGetTime()
            engineResources.set(DeltaTime(currentTime - previousTime))
            previousTime = currentTime

            val physicsResources = ResourcesView(scene.sceneResources, engineResources)
            scene.defaultSystems.forEach(
                scene.runSystem(
                    System::executePhysics,
                    physicsResources,
                    eventQueuesFor(physicsEvents)
                )
            )

            val networkResources = ResourcesView(scene.sceneResources, engineResources)
            scene.defaultSystems.forEach(
                scene.runSystem(
                    System::executeNetwork,
                    networkResources,
                    eventQueuesFor(networkEvents)
                )
            )

            physicsEvents.finish()
            networkEvents.finish()
            renderEvents.finish()
        }
    }

    override fun exit() {
        GLFW.glfwTerminate()

        exitProcess(0)
    }
}