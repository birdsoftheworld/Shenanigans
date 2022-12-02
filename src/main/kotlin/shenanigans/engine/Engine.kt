package shenanigans.engine

import com.esotericsoftware.kryonet.Client
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30C.glClearColor
import shenanigans.engine.ecs.Resources
import shenanigans.engine.events.Event
import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.StateMachineResource
import shenanigans.engine.events.control.ControlEvent
import shenanigans.engine.events.control.ExitEvent
import shenanigans.engine.events.control.SceneChangeEvent
import shenanigans.engine.events.control.UpdateDefaultSystemsEvent
import shenanigans.engine.graphics.Renderer
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.scene.Scene
import shenanigans.engine.window.Window
import shenanigans.engine.window.WindowResource
import shenanigans.engine.window.events.KeyboardState
import shenanigans.engine.window.events.MouseState

abstract class Engine(initScene: Scene) {
    protected var scene: Scene = initScene
    protected val resources = Resources()

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