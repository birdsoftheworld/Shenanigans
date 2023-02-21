package shenanigans.engine.events

interface StateMachine {
    fun transitionPhysics(queue: EventQueue) {}
    fun transitionNetwork(queue: EventQueue) {}
    fun transitionRender(queue: EventQueue) {}
}