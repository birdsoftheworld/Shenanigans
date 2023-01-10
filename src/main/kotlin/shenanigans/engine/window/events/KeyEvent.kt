package shenanigans.engine.window.events

import shenanigans.engine.ecs.Resource
import shenanigans.engine.events.Event
import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.StateMachine
import shenanigans.engine.window.Key
import shenanigans.engine.window.KeyAction
import shenanigans.engine.window.KeyModifier

data class KeyEvent(val key: Key, val action: KeyAction, val modifiers: KeyModifier) : Event {
    companion object {
        fun wrappedGlfwCallback(inner: (KeyEvent) -> Unit): (Long, Int, Int, Int, Int) -> Unit {
            return { window, key, scancode, action, mods ->
                inner(
                    KeyEvent(
                        Key(key),
                        KeyAction(action),
                        KeyModifier(mods)
                    )
                )
            }
        }
    }
}

class KeyboardState : Resource, StateMachine {
    private val pressed: MutableMap<Key, Boolean> = mutableMapOf()

    override fun transition(queue: EventQueue) {
        queue.iterate<KeyEvent>().forEach { event ->
            pressed[event.key] = event.action == KeyAction.PRESS || event.action == KeyAction.REPEAT
        }
    }

    fun isPressed(key: Key): Boolean {
        return pressed[key] ?: false
    }
}