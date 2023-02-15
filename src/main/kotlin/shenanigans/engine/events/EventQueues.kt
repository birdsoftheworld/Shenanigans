package shenanigans.engine.events

import shenanigans.engine.net.NetworkEventQueue

data class EventQueues<Q : EventQueue?>(
    val physics: LocalEventQueue,
    val network: NetworkEventQueue,
    val render: LocalEventQueue,
    val own: Q,
)