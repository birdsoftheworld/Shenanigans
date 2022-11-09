package shenanigans.engine.window.events

import org.joml.Vector2d
import shenanigans.engine.window.KeyModifier
import shenanigans.engine.window.MouseButton
import shenanigans.engine.window.MouseButtonAction

sealed class MouseEvent

data class MousePositionEvent(val position: Vector2d) : MouseEvent() {
    companion object {
        fun wrappedGlfwCallback(inner: (MousePositionEvent) -> Unit): (Long, Double, Double) -> Unit {
            return { window, xpos, ypos ->
                inner(
                    MousePositionEvent(
                        Vector2d(xpos, ypos)
                    )
                )
            }
        }
    }
}

data class MouseButtonEvent(val button: MouseButton, val action: MouseButtonAction, val modifiers: KeyModifier) {
    companion object {
        fun wrappedGlfwCallback(inner: (MouseButtonEvent) -> Unit): (Long, Int, Int, Int) -> Unit {
            return { window, button, action, mods ->
                inner(
                    MouseButtonEvent(
                        MouseButton(button),
                        MouseButtonAction(action),
                        KeyModifier(mods)
                    )
                )
            }
        }
    }
}

data class MouseScrollEvent(val offset: Vector2d) : MouseEvent() {
    companion object {
        fun wrappedGlfwCallback(inner: (MouseScrollEvent) -> Unit): (Long, Double, Double) -> Unit {
            return { window, xoffset, yoffset ->
                inner(
                    MouseScrollEvent(
                        Vector2d(xoffset, yoffset)
                    )
                )
            }
        }
    }
}