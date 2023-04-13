package shenanigans.engine

import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30C
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.ecs.System
import shenanigans.engine.graphics.Renderer
import shenanigans.engine.net.Client
import shenanigans.engine.net.Network
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.scene.Scene
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.util.camera.OrthoCamera
import shenanigans.engine.window.Window
import shenanigans.engine.window.WindowResource
import shenanigans.engine.window.events.KeyboardState
import shenanigans.engine.window.events.MouseState
import kotlin.system.exitProcess
import java.lang.System as JSystem

class ClientEngine(initScene: Scene, networkImpl: Network = Network(Client())) : Engine(initScene = initScene, networkImpl) {
    private lateinit var window: Window

    override fun init() {
        window = Window("game", 640, 640)

        window.onEvent { e -> physicsEvents.queueLater(e) }

        engineResources.set(WindowResource(window))
        engineResources.set(KeyboardState())
        engineResources.set(MouseState())

        engineResources.set(CameraResource(OrthoCamera()))
    }

    override fun loop() {
        GL.createCapabilities()
        Renderer.init()

        GL30C.glClearColor(0.5f, 1.0f, 0.5f, 0.5f)

        window.onResize { _, _ ->
            Renderer.renderGame(window, scene, engineResources, eventQueuesFor(renderEvents))
        }

        if (JSystem.getProperty("no_network") == null) {
            network.impl.connect()
        }

        var previousTime = GLFW.glfwGetTime()

        while (!window.shouldClose) {
            GLFW.glfwPollEvents()

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

            Renderer.renderGame(window, scene, engineResources, eventQueuesFor(renderEvents))

            physicsEvents.finish()
            networkEvents.finish()
            renderEvents.finish()
        }
    }

    override fun exit() {
        Renderer.discard()

        GLFW.glfwTerminate()

        exitProcess(0)
    }
}