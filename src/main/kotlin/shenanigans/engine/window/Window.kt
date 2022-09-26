package shenanigans.engine.window

import org.joml.Vector2d
import org.joml.Vector2i
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack
import java.nio.IntBuffer

class Window(title: String, width: Int, height: Int) {
    private val windowId: Long

    init {
        check(glfwInit()) { "Failed to initialize GLFW" }
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE)
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
        windowId = glfwCreateWindow(width, height, title, 0, 0)
        if (windowId == 0L) {
            throw RuntimeException("Failed to create window")
        }
        setKeyCallback { key: Int, _: Int, _: Int, _: Int ->
            if (key == GLFW_KEY_ESCAPE) {
                setShouldClose(true)
            }
        }
        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)
            glfwGetWindowSize(windowId, pWidth, pHeight)
            val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
            setPosition((videoMode!!.width() - pWidth[0]) / 2, (videoMode.height() - pHeight[0]) / 2)
        }
        glfwMakeContextCurrent(windowId)
        glfwSwapInterval(1) // vsync
        glfwShowWindow(windowId)
    }

    private fun setKeyCallback(callback: (Int, Int, Int, Int) -> Unit) {
        glfwSetKeyCallback(windowId) { _: Long, key: Int, scancode: Int, actions: Int, mods: Int -> callback(key, scancode, actions, mods) }
    }

    fun setShouldClose(value: Boolean) {
        glfwSetWindowShouldClose(windowId, value)
    }

    fun shouldClose(): Boolean {
        return glfwWindowShouldClose(windowId)
    }

    fun setPosition(x: Int, y: Int) {
        glfwSetWindowPos(windowId, x, y)
    }

    fun getPosition() : Vector2i{
        val xPos = IntArray(1)
        val yPos = IntArray(1)
        glfwGetWindowPos(windowId, xPos, yPos)
        return Vector2i(xPos[0], yPos[0])
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