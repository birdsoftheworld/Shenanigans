package shenanigans.engine

import org.lwjgl.glfw.GLFW
import shenanigans.engine.ecs.Resources
import shenanigans.engine.events.Event
import shenanigans.engine.init.EngineOptions
import shenanigans.engine.scene.Scene

abstract class Engine(initScene: Scene, val options: EngineOptions = EngineOptions()) {
    protected var scene: Scene = initScene
    internal val engineResources = Resources()

    protected var unprocessedEvents = mutableListOf<Event>()

    fun run() {
        if(!GLFW.glfwInit()) throw RuntimeException("Failed to initialize GLFW")
        init()
        loop()
        GLFW.glfwTerminate()
    }

    abstract fun init()

    fun queueEvent(event: Event) {
        unprocessedEvents.add(event)
    }

    abstract fun loop()
}