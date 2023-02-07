package shenanigans.engine.events

import shenanigans.engine.ecs.Resource

class EventQueue internal constructor() : Resource {
    @PublishedApi internal var events = listOf<Event>()
    private var deferredEvents = mutableListOf<Event>()

    inline fun <reified T : Event> iterate(): Sequence<T> {
        return events.asSequence().filter((T::class)::isInstance).map { it as T }
    }

    fun queueLater(event: Event) {
        deferredEvents.add(event)
    }

    fun finish() {
        // shhhh pretend this is atomic
        events = deferredEvents
        deferredEvents = mutableListOf()
    }
}