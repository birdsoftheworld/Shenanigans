package shenanigans.game.network

import com.esotericsoftware.kryonet.Connection
import shenanigans.engine.ecs.Component
import shenanigans.engine.events.Event

class Synchronized : Component {
    val connected : Boolean = false
}

class ConnectionEvent(val connection: Connection) : Event

annotation class ClientOnly