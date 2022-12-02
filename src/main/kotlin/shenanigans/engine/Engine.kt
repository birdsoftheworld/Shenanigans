package shenanigans.engine

import org.lwjgl.glfw.GLFW
import shenanigans.engine.ecs.Resources
import shenanigans.engine.events.Event
import shenanigans.engine.graphics.api.CameraResource
import shenanigans.engine.scene.Scene
import shenanigans.engine.util.OrthoCamera

abstract class Engine(initScene: Scene) {
    protected var scene: Scene = initScene
    internal val engineResources = Resources()

    protected var unprocessedEvents = mutableListOf<Event>();

    fun run() {
        if(!GLFW.glfwInit()) throw RuntimeException("Failed to initialize GLFW")
        init()
        scene.sceneResources.set(CameraResource(OrthoCamera()))
        loop()
        GLFW.glfwTerminate()
    }

    abstract fun init()

    fun queueEvent(event: Event) {
        unprocessedEvents.add(event)
    }

    abstract fun loop()
}