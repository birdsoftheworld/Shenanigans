package shenanigans.engine.ui

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector4f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.Event
import shenanigans.engine.events.EventQueue
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.util.Transform
import shenanigans.engine.util.isPointInside
import shenanigans.engine.util.setToTransform
import shenanigans.engine.window.MouseButtonAction
import shenanigans.engine.window.events.MouseButtonEvent
import shenanigans.engine.window.events.MouseState
import java.awt.event.MouseEvent
import java.util.Vector
import kotlin.reflect.KClass

class Button : Component {}

class ButtonSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return listOf(Button::class, Shape::class, Transform::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        resources.get<EventQueue>().iterate<MouseButtonEvent>().forEach { event ->
            if(event.action == MouseButtonAction.PRESS) {
                val mousePos = resources.get<MouseState>().position()

                entities.forEach { button ->
                    if(button.component<Shape>().get().isPointInside(mousePos, button.component<Transform>().get())) {
                        println("Buttton Pressed!!!!")
                    }
                }
            }
        }
    }
}
