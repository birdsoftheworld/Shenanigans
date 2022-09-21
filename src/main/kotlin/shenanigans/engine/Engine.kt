package shenanigans.engine

import shenanigans.engine.window.Window
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11

class Engine {
    private var window: Window? = null

    private fun init() {
        window = Window("game", 640, 480)
    }

    fun run() {
        init()
        GL.createCapabilities()
        GL11.glClearColor(0.5f, 1.0f, 0.5f, 0.5f)
        while (!window!!.shouldClose()) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT) // clear the framebuffer
            window!!.swapBuffers()
            GLFW.glfwPollEvents()
        }
        GLFW.glfwTerminate()
    }

    fun loop() {}

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Engine().run()
        }
    }
}