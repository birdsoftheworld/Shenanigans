package shenanigans.engine.window.events

import org.joml.Vector2f
import org.joml.Vector2fc
import shenanigans.engine.ecs.Resource
import shenanigans.engine.events.Event
import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.StateMachineResource
import shenanigans.engine.window.KeyModifier
import shenanigans.engine.window.MouseButton
import shenanigans.engine.window.MouseButtonAction

sealed class MouseEvent : Event

data class MousePositionEvent(val position: Vector2fc) : MouseEvent() {
    companion object {
        fun wrappedGlfwCallback(inner: (MousePositionEvent) -> Unit): (Long, Double, Double) -> Unit {
            return { window, xpos, ypos ->
                inner(
                    MousePositionEvent(
                        Vector2f(xpos.toFloat(), ypos.toFloat())
                    )
                )
            }
        }
    }
}

data class CursorPresenceEvent(val present: Boolean) : MouseEvent() {
    companion object {
        fun wrappedGlfwCallback(inner: (CursorPresenceEvent) -> Unit): (Long, Boolean) -> Unit {
            return { window, present ->
                inner(
                    CursorPresenceEvent(
                        present
                    )
                )
            }
        }
    }
}

data class MouseButtonEvent(val button: MouseButton, val action: MouseButtonAction, val modifiers: KeyModifier) :
    MouseEvent() {
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

data class MouseScrollEvent(val offset: Vector2fc) : MouseEvent() {
    companion object {
        fun wrappedGlfwCallback(inner: (MouseScrollEvent) -> Unit): (Long, Double, Double) -> Unit {
            return { window, xoffset, yoffset ->
                inner(
                    MouseScrollEvent(
                        Vector2f(xoffset.toFloat(), yoffset.toFloat())
                    )
                )
            }
        }
    }
}

class MouseState : Resource, StateMachineResource {
    private val position = Vector2f()
    private var cursorPresent = false
    private val pressed = mutableMapOf<MouseButton, Boolean>()

    override fun transition(queue: EventQueue) {
        queue.iterate<MouseEvent>().forEach { event ->
            when (event) {
                is MousePositionEvent -> {
                    position.set(event.position)
                }

                is CursorPresenceEvent -> {
                    cursorPresent = event.present
                }

                is MouseButtonEvent -> {
                    pressed[event.button] = event.action == MouseButtonAction.PRESS
                }

                else -> {}
            }
        }
    }

    fun position(): Vector2fc {
        return this.position
    }

    fun cursorPresent(): Boolean {
        return this.cursorPresent
    }

    fun isPressed(button: MouseButton): Boolean {
        return pressed.getOrDefault(button, false)
    }
}
