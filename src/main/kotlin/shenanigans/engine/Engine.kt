package shenanigans.engine

import org.joml.Vector2d
import shenanigans.engine.window.Window
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30C.*
import java.util.Vector

class Engine {
    private var window: Window? = null

    private fun init() {
        window = Window("game", 640, 480)
    }

    fun run() {
        init()
        loop()
        glfwTerminate()
    }

    fun loop() {
        GL.createCapabilities()

        glClearColor(0.5f, 1.0f, 0.5f, 0.5f)

        while (!window!!.shouldClose()) {
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer
            window!!.swapBuffers()
            glfwPollEvents()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Engine().run()
        }
    }
}