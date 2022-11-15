package shenanigans.engine.events

import shenanigans.engine.ecs.Resource

interface StateMachineResource : Resource {
    fun transition(queue: EventQueue)
}