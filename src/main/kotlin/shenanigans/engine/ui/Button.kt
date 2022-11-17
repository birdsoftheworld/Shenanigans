package shenanigans.engine.ui

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector4f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.Event
import shenanigans.engine.events.EventQueue
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.util.Transform
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
            if(event.action == MouseButtonAction.RELEASE) {
                val mousePos = resources.get<MouseState>().position

                entities.forEach { button ->
                    if(pointInside(button.component<Shape>().get(), button.component<Transform>().get(), mousePos)) {
                        println("Buttton Pressed!!!!")
                    }
                }
            }
        }
    }

    private fun pointInside(shape: Shape, transform: Transform, point: Vector2f) : Boolean {
        val transformMatrix = Matrix4f()

        transformMatrix.setToTransform(transform.position, transform.rotation, transform.scale)

        val transformedVertices = Array(shape.vertices.size) { Vector2f() }

        for (i in 0 until shape.vertices.size) {
            val vertex = Vector4f(shape.vertices[i], 0f, 1f).mul(transformMatrix)
            transformedVertices[i].x = vertex.x
            transformedVertices[i].y = vertex.y
        }

        var count = 0
        for (i in 0 until shape.vertices.size) {
            if(pointProjectionIntersectsLine(
                    point,
                    Pair(transformedVertices[i], transformedVertices[(i + 1) % shape.vertices.size]))
            ) {
                count ++
            }
        }
        return count != 0 && count % 2 == 0
    }

    private fun pointProjectionIntersectsLine(point: Vector2f, line: Pair<Vector2f, Vector2f>) : Boolean {
        return (((line.first.y > point.y && line.second.y < point.y) ||
            (line.first.y < point.y && line.second.y > point.y)) &&
            point.x < line.first.x || point.x < line.second.x)
    }
}
