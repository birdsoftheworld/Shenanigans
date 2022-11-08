package shenanigans.engine.events

import shenanigans.engine.ecs.Resource

class EventQueue(@PublishedApi internal val events: Sequence<Event>) : Resource {
    inline fun <reified T: Event> iterate(): Sequence<Event> {
        return events.filter((T::class)::isInstance)
    }
}