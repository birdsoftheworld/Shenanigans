package shenanigans.engine.window.events

import shenanigans.engine.events.Event
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
