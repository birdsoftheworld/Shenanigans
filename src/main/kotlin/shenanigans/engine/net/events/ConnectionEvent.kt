package shenanigans.engine.net.events

import shenanigans.engine.events.Event
import shenanigans.engine.net.MessageEndpoint

class ConnectionEvent(val endpoint: MessageEndpoint?, val type: ConnectionEventType) : Event

enum class ConnectionEventType {
    Connect,
    Disconnect
}