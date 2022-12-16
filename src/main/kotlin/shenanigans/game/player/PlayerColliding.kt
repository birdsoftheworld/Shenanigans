package shenanigans.game.player

import shenanigans.engine.ecs.Resource
import shenanigans.engine.events.Event
import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.StateMachineResource

class PlayerColliding : Resource, StateMachineResource{

    override fun transition(queue: EventQueue) {
        queue.iterate<PlayerCollide>().forEach { event ->
            event.touchGrass()
        }
    }

}