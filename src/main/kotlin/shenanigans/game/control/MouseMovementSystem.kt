package shenanigans.game.control

import org.joml.Vector2f
import org.joml.Vector3f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.net.ClientOnly
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.util.isPointInside
import shenanigans.engine.window.Key
import shenanigans.engine.window.MouseButtonAction
import shenanigans.engine.window.events.KeyboardState
import shenanigans.engine.window.events.MouseButtonEvent
import shenanigans.engine.window.events.MouseScrollEvent
import shenanigans.engine.window.events.MouseState
import shenanigans.game.level.block.OscillatingBlock
import kotlin.math.round
import kotlin.reflect.KClass

class MouseMovementSystem : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(setOf(MouseDraggable::class, Transform::class))

        entities.forEach { entity ->
            val mouseDraggable = entity.component<MouseDraggable>().get()
            if (mouseDraggable.grabbed) {
                val transform = entity.component<Transform>().get()
                val position = resources.get<MouseState>().position()
                val transformedPosition = resources.get<CameraResource>().camera!!.untransformPoint(
                    Vector3f(
                        position,
                        0f
                    )
                )
                val dragOffset = mouseDraggable.dragOffset
                val x = dragOffset.x
                val y = dragOffset.y
                var scroll: Float
                eventQueues.own.receive(MouseScrollEvent::class).forEach{ event ->
                    scroll = event.offset.y()
                    if(scroll > 0){
                        transform.rotation -= (Math.PI/2).toFloat()
                        dragOffset.x = y
                        dragOffset.y = -x
                        if(entity.componentOpt<OscillatingBlock>() != null){
                            entity.component<OscillatingBlock>().get().rotate(false)
                        }
                    }
                    if(scroll < 0){
                        transform.rotation += (Math.PI/2).toFloat()
                        dragOffset.x = -y
                        dragOffset.y = x
                        if(entity.componentOpt<OscillatingBlock>() != null){
                            entity.component<OscillatingBlock>().get().rotate(true)
                        }
                    }
                }

                transform.position.set(transformedPosition.x() + mouseDraggable.dragOffset.x(), transformedPosition.y() + mouseDraggable.dragOffset.y(),transform.position.z)
                entity.component<Transform>().mutate()
            }
        }

        eventQueues.own.receive(MouseButtonEvent::class).forEach { event ->
            entities.forEach { entity ->
                val transform = entity.component<Transform>().get()
                val mousePosition = resources.get<MouseState>().position()
                val transformedPosition =
                    resources.get<CameraResource>().camera!!.untransformPoint(Vector3f(mousePosition, 0f))
                val mouseDraggable = entity.component<MouseDraggable>().get()
                if (entity.component<Collider>().get().polygon.isPointInside(
                        Vector2f(
                            transformedPosition.x,
                            transformedPosition.y
                        ), transform)
                ) {
                    if(resources.get<KeyboardState>().isPressed(Key.Q)){
                        lifecycle.del(entity.id)
                    }
                    if(event.action == MouseButtonAction.PRESS){

                        mouseDraggable.dragOffset.x = transform.position.x - transformedPosition.x()
                        mouseDraggable.dragOffset.y = transform.position.y - transformedPosition.y()
                        mouseDraggable.grab()
                    }
                }
                if (entity.component<MouseDraggable>().get().grabbed) {
                    if(event.action == MouseButtonAction.RELEASE) {
                        mouseDraggable.drop()
                        transform.position.x = round(transform.position.x / 50) * 50
                        transform.position.y = round(transform.position.y / 50) * 50
                        if (entity.componentOpt<OscillatingBlock>() != null) {
                            entity.component<OscillatingBlock>().get().reset()
                            entity.component<OscillatingBlock>().get()
                                .newStartPos(transform.position.x, transform.position.y)
                        }
                    }
                }
            }
        }
    }
}

@ClientOnly
class MouseDraggable(var grabbed: Boolean, var dragOffset: Vector2f) : Component {
    fun grab() {
        this.grabbed = true
    }

    fun drop() {
        this.grabbed = false
    }
}