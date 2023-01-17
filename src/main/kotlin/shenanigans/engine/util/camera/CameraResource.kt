package shenanigans.engine.util.camera

import shenanigans.engine.ecs.Resource
import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.StateMachine

class CameraResource(camera: Camera? = null) : Resource, StateMachine {
    var camera: Camera? = camera
        private set

    private var deferredValue: Camera? = camera

    fun setActiveCamera(camera: Camera?) {
        deferredValue = camera
    }

    override fun transition(queue: EventQueue) {
        camera = deferredValue
    }
}