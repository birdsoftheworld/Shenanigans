package shenanigans.engine.net

import shenanigans.engine.events.Event

sealed class Message(
    val delivery: MessageDelivery = MessageDelivery.UnreliableUnordered,
    var sender: MessageEndpoint? = null,
    var recipient: MessageEndpoint? = null
)

enum class MessageDelivery {
    ReliableOrdered, UnreliableUnordered,
}

sealed class MessageEndpoint {
    data class Client(val id: Int) : MessageEndpoint()
    object Server : MessageEndpoint()
}

class EventMessage<E : Event>(
    val event: E,
    delivery: MessageDelivery = MessageDelivery.UnreliableUnordered,
    sender: MessageEndpoint? = null,
    recipient: MessageEndpoint? = null
) : Message(delivery = delivery, sender = sender, recipient = recipient)
