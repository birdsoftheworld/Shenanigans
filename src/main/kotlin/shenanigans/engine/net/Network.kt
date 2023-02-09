package shenanigans.engine.net

import shenanigans.engine.events.EventQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Network(internal val impl: NetworkImplementation) {
    internal var receivedMessages: MutableList<Message> = mutableListOf()
    internal val receiveLock: ReentrantLock = ReentrantLock()

    init {
        impl.registerListener { msg ->
            receiveLock.withLock {
                receivedMessages.add(msg)
            }
        }
    }

    fun createEventQueue(): EventQueue {
        return NetworkedEventQueue(this)
    }
}

private class NetworkedEventQueue(val network: Network) : EventQueue() {
    override fun finish() {
        network.receiveLock.withLock {
            receivedEvents =
                network.receivedMessages.filterIsInstance<EventMessage<*>>().map { it.event }
        }

        sentEvents.forEach {
            network.impl.sendMessage(EventMessage(it))
        }
    }
}