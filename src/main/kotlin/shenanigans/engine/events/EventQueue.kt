package shenanigans.engine.events

import shenanigans.engine.ecs.Resource

abstract class EventQueue {
    var receivedEvents: List<Event> = emptyList()
    var sentEvents: MutableList<Event> = mutableListOf()


    inline fun <reified T : Event> iterate(): Sequence<T> {
        return receivedEvents.asSequence().filter((T::class)::isInstance).map { it as T }
    }

    fun queueLater(event: Event) {
        sentEvents.add(event)
    }

    abstract fun finish()
}

class LocalEventQueue : EventQueue() {
    override fun finish() {
        receivedEvents = sentEvents
        sentEvents = mutableListOf()
    }
}
