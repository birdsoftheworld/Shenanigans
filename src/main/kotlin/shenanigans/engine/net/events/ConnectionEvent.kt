package shenanigans.engine.net.events

import com.esotericsoftware.kryonet.Connection
import shenanigans.engine.events.Event
import shenanigans.engine.net.MessageEndpoint
import java.util.UUID

class ConnectionEvent(val endpoint: MessageEndpoint?, val type: ConnectionEventType) : Event

enum class ConnectionEventType {
    Connect,
    Disconnect
}