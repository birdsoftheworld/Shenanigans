package shenanigans.game.network

import com.esotericsoftware.kryonet.Connection
import shenanigans.engine.events.Event

class ConnectionEvent(val connection: Connection) : Event