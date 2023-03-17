package shenanigans.engine.net.events

import com.esotericsoftware.kryonet.Connection
import shenanigans.engine.events.Event
import java.util.UUID

class ConnectionEvent(val connectionId: Int?, val type: ConnectionEventType) : Event

enum class ConnectionEventType {
    Connect,
    Disconnect
}