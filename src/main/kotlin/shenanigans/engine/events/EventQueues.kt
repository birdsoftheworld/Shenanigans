package shenanigans.engine.events

data class EventQueues(
    val physics: EventQueue,
    val network: EventQueue,
    val render: EventQueue,
    val own: EventQueue
)

fun fakeEventQueues(): EventQueues {
    return EventQueues(
        physics = LocalEventQueue(),
        network = LocalEventQueue(),
        render = LocalEventQueue(),
        own = LocalEventQueue()
    )
}