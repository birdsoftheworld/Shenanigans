package shenanigans.engine.events

import kotlin.reflect.KClass

abstract class EventQueue {
    abstract val received: List<Event>

    fun <E : Event> receive(cl: KClass<E>): Sequence<E> {
        return received.asSequence().filterIsInstance(cl.java)
    }

    abstract fun queueLater(event: Event)

    abstract fun finish()
}

class LocalEventQueue : EventQueue() {
    override var received: List<Event> = emptyList()
    private var sent: MutableList<Event> = mutableListOf()

    override fun queueLater(event: Event) {
        sent.add(event)
    }

    override fun finish() {
        received = sent
        sent = mutableListOf()
    }
}
