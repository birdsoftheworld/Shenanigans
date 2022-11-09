package shenanigans.engine.events

import shenanigans.engine.ecs.Resource

class EventQueue(@PublishedApi internal val events: Sequence<Event>) : Resource {
    inline fun <reified T: Event> iterate(): Sequence<T> {
        return events.filter((T::class)::isInstance).map { it as T }
    }
}