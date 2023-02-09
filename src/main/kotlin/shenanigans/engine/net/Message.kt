package shenanigans.engine.net

import shenanigans.engine.events.Event

sealed class Message(val delivery: MessageDelivery = MessageDelivery.UnreliableUnordered)

enum class MessageDelivery {
    ReliableOrdered,
    UnreliableUnordered,
}

class EventMessage<E : Event>(val event: E, reliable: MessageDelivery = MessageDelivery.UnreliableUnordered) :
    Message(reliable)
