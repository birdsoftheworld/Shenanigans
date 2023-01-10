package shenanigans.engine.events

interface StateMachine {
    fun transition(queue: EventQueue)
}