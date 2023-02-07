package shenanigans.engine.net.events

import com.esotericsoftware.kryonet.Connection
import shenanigans.engine.events.Event

class ConnectionEvent(val connection: Connection?, val connectionType: ConnectionType) : Event

enum class ConnectionType {
    Connect,
    Disconnect
}