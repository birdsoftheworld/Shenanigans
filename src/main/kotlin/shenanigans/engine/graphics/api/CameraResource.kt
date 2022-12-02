package shenanigans.engine.graphics.api

import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.StateMachineResource
import shenanigans.engine.util.Camera

class CameraResource(camera: Camera? = null) : StateMachineResource {
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