package shenanigans.engine.window

import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack

class Window(title: String, width: Int, height: Int) {
    private val windowId: Long

    init {
        check(GLFW.glfwInit()) { "Failed to initialize GLFW" }
        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE)
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)
        windowId = GLFW.glfwCreateWindow(width, height, title, 0, 0)
        if (windowId == 0L) {
            throw RuntimeException("Failed to create window")
        }
        setKeyCallback { key: Int, _: Int, _: Int, _: Int ->
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                setShouldClose(true)
            }
        }
        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)
            GLFW.glfwGetWindowSize(windowId, pWidth, pHeight)
            val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())
            setPosition((vidmode!!.width() - pWidth[0]) / 2, (vidmode.height() - pHeight[0]) / 2)
        }
        GLFW.glfwMakeContextCurrent(windowId)
        GLFW.glfwSwapInterval(1) // vsync
        GLFW.glfwShowWindow(windowId)
    }

    private fun setKeyCallback(callback: (Int, Int, Int, Int) -> Unit) {
        GLFW.glfwSetKeyCallback(windowId) { _: Long, key: Int, scancode: Int, actions: Int, mods: Int -> callback(key, scancode, actions, mods) }
    }

    fun setShouldClose(value: Boolean) {
        GLFW.glfwSetWindowShouldClose(windowId, value)
    }

    fun shouldClose(): Boolean {
        return GLFW.glfwWindowShouldClose(windowId)
    }

    fun setPosition(x: Int, y: Int) {
        GLFW.glfwSetWindowPos(windowId, x, y)
    }

    fun swapBuffers() {
        GLFW.glfwSwapBuffers(windowId)
    }

    fun isKeyPressed(key: Int): Boolean {
        return GLFW.glfwGetKey(windowId, key) == GLFW.GLFW_PRESS
    }

    fun discard() {
        Callbacks.glfwFreeCallbacks(windowId)
        GLFW.glfwDestroyWindow(windowId)
    }
}