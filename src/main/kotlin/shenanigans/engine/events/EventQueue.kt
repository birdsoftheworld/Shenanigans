package shenanigans.engine.events

import shenanigans.engine.ecs.Resource

class EventQueue internal constructor(
    @PublishedApi internal val events: Iterable<Event>,
    private val queueLaterCb: (Event) -> Unit
) : Resource {
    inline fun <reified T : Event> iterate(): Sequence<T> {
        return events.asSequence().filter((T::class)::isInstance).map { it as T }
    }

    fun queueLater(event: Event) {
        queueLaterCb(event)
    }
}