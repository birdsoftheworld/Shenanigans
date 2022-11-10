package shenanigans.engine.window

import org.lwjgl.glfw.GLFW

@JvmInline
value class MouseButton internal constructor(val code: Int) {
    companion object {
        val LEFT = MouseButton(GLFW.GLFW_MOUSE_BUTTON_LEFT)
        val MIDDLE = MouseButton(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
        val RIGHT = MouseButton(GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        val BUTTON_1 = MouseButton(GLFW.GLFW_MOUSE_BUTTON_1)
        val BUTTON_2 = MouseButton(GLFW.GLFW_MOUSE_BUTTON_2)
        val BUTTON_3 = MouseButton(GLFW.GLFW_MOUSE_BUTTON_3)
        val BUTTON_4 = MouseButton(GLFW.GLFW_MOUSE_BUTTON_4)
        val BUTTON_5 = MouseButton(GLFW.GLFW_MOUSE_BUTTON_5)
        val BUTTON_6 = MouseButton(GLFW.GLFW_MOUSE_BUTTON_6)
        val BUTTON_7 = MouseButton(GLFW.GLFW_MOUSE_BUTTON_7)
        val BUTTON_8 = MouseButton(GLFW.GLFW_MOUSE_BUTTON_8)
        val BUTTON_LAST = MouseButton(GLFW.GLFW_MOUSE_BUTTON_LAST)
    }
}

@JvmInline
value class MouseButtonAction internal constructor(val code: Int) {
    companion object {
        val PRESS = MouseButtonAction(GLFW.GLFW_PRESS)
        val RELEASE = MouseButtonAction(GLFW.GLFW_RELEASE)
    }
}