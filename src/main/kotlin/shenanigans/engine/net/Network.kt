package shenanigans.engine.net

import shenanigans.engine.events.Event
import shenanigans.engine.events.EventQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

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

    fun createEventQueue(): NetworkEventQueue {
        return NetworkEventQueue(this)
    }
}

class NetworkEventQueue internal constructor(val network: Network) : EventQueue() {
    var receivedMessages: List<EventMessage<*>> = emptyList()

    override val received: List<Event>
        get() = receivedMessages.map { it.event }

    fun <E : Event> receiveNetwork(cl: KClass<E>): Sequence<EventMessage<E>> {
        return receivedMessages.asSequence().filter { it.event::class == cl }.map { it as EventMessage<E> }
    }

    override fun queueLater(event: Event) {
        network.impl.sendMessage(EventMessage(event))
    }

    fun queueNetwork(event: Event, delivery: MessageDelivery) {
        network.impl.sendMessage(EventMessage(event, delivery))
    }

    override fun finish() {
        network.receiveLock.withLock {
            receivedMessages =
                network.receivedMessages.filterIsInstance(EventMessage::class.java)
            network.receivedMessages.clear()
        }
    }
}