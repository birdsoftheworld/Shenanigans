package shenanigans.engine

import org.lwjgl.glfw.GLFW.*
import shenanigans.engine.ecs.Resources
import shenanigans.engine.events.Event
import shenanigans.engine.scene.Scene

abstract class Engine(initScene: Scene) {
    protected var scene: Scene = initScene
    internal val engineResources = Resources()

    protected var unprocessedEvents = mutableListOf<Event>();

    fun run() {
        init()
        loop()
        glfwTerminate()
    }

    abstract fun init()

    fun queueEvent(event: Event) {
        unprocessedEvents.add(event)
    }

    abstract fun loop()
}