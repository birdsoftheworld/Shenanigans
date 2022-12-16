package shenanigans.game.player

import shenanigans.engine.events.Event
import shenanigans.engine.window.events.MouseEvent

data class PlayerCollide(var onGround: Boolean) : Event {
    fun touchGrass(){
        onGround = true
    }

    fun goInside(){
        onGround = false
    }
}