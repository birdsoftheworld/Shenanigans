package shenanigans.engine.window

import org.joml.Vector2i
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL30C.GL_TRUE
import org.lwjgl.system.MemoryStack

class Window(title: String, width: Int, height: Int) {
    private val windowId: Long

    var shouldClose: Boolean
        get() = glfwWindowShouldClose(windowId)
        set(value) = glfwSetWindowShouldClose(windowId, value)

    var position: Vector2i
        get() {
            val xPos = IntArray(1)
            val yPos = IntArray(1)
            glfwGetWindowPos(windowId, xPos, yPos)
            return Vector2i(xPos[0], yPos[0])
        }
        set(value) = glfwSetWindowPos(windowId, value.x, value.y)

    val size: Vector2i
        get() {
            val width = IntArray(1)
            val height = IntArray(1)
            glfwGetWindowSize(windowId, width, height)
            return Vector2i(width[0], height[0])
        }

    val width: Int
        get() = size.x

    val height: Int
        get() = size.y

    init {
        check(glfwInit()) { "Failed to initialize GLFW" }
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
        glfwWindowHint(GLFW_SAMPLES, 8)
        windowId = glfwCreateWindow(width, height, title, 0, 0)
        if (windowId == 0L) {
            throw RuntimeException("Failed to create window")
        }
        setKeyCallback { key: Int, _: Int, _: Int, _: Int ->
            if (key == GLFW_KEY_ESCAPE) {
                shouldClose = true
            }
        }
        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)
            glfwGetWindowSize(windowId, pWidth, pHeight)
            val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
            position = Vector2i((videoMode!!.width() - pWidth[0]) / 2, (videoMode.height() - pHeight[0]) / 2)
        }
        glfwMakeContextCurrent(windowId)
        glfwSwapInterval(0) // vsync
        glfwShowWindow(windowId)
    }

    private fun setKeyCallback(callback: (Int, Int, Int, Int) -> Unit) {
        glfwSetKeyCallback(windowId) { _: Long, key: Int, scancode: Int, actions: Int, mods: Int -> callback(key, scancode, actions, mods) }
    }

    fun swapBuffers() {
        glfwSwapBuffers(windowId)
    }

    fun isKeyPressed(key: Int): Boolean {
        return glfwGetKey(windowId, key) == GLFW_PRESS
    }

    fun discard() {
        Callbacks.glfwFreeCallbacks(windowId)
        glfwDestroyWindow(windowId)
    }
}