package shenanigans.engine.events

data class EventQueues(
    val physics: EventQueue,
    val network: EventQueue,
    val render: EventQueue,
    val own: EventQueue
)

fun emptyEventQueues(): EventQueues {
    return EventQueues(
        physics = EventQueue(),
        network = EventQueue(),
        render = EventQueue(),
        own = EventQueue()
    )
}