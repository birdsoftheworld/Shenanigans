package shenanigans.engine.net.events

import com.esotericsoftware.kryonet.Connection
import shenanigans.engine.events.Event

class ConnectionEvent(connection: Connection?, connectionType: ConnectionType) : Event

enum class ConnectionType {
    Connect,
    Disconnect
}