package shenanigans.engine.resources

import shenanigans.engine.ecs.Resource
import shenanigans.engine.input.Key
import shenanigans.engine.window.Window

class KeyboardInput(private val window: Window) : Resource {
    fun isDown(key: Key): Boolean {
        return window.isKeyPressed(key.code)
    }
}