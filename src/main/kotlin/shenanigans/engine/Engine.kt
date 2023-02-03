package shenanigans.engine

import org.lwjgl.glfw.GLFW
import shenanigans.engine.ecs.Resources
import shenanigans.engine.events.Event
import shenanigans.engine.scene.Scene
import java.util.concurrent.locks.ReentrantLock

abstract class Engine(initScene: Scene) {
    protected var scene: Scene = initScene
    internal val engineResources = Resources()

    protected val eventLock = ReentrantLock()
    protected var unprocessedEvents = mutableListOf<Event>()

    fun run() {
        if(!GLFW.glfwInit()) throw RuntimeException("Failed to initialize GLFW")
        init()
        loop()
        GLFW.glfwTerminate()
    }

    abstract fun init()

    /**
     * Queue an event with no regard for the lock state. Do not use on a different thread than the game loop thread.
     */
    fun unsafeQueueEvent(event: Event) {
        unprocessedEvents.add(event)
    }

    /**
     * Wait to acquire the event lock, then queue an event.
     */
    fun queueEvent(event: Event) {
        eventLock.lock()
        unprocessedEvents.add(event)
        eventLock.unlock()
    }

    abstract fun loop()
}