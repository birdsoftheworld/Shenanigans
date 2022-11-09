package shenanigans

import org.joml.Vector2f
import shenanigans.engine.Engine
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.scene.Scene
import shenanigans.engine.util.Transform
import shenanigans.engine.window.events.MousePositionEvent
import kotlin.reflect.KClass

fun main() {
    Engine(testScene()).run()
}

fun testScene(): Scene {
    val scene = Scene()

    // NOTE: in the future, this will not be the recommended way to populate a scene
    //       instead, the engine will have a facility for running systems once
    //       which will be used with a canonical "AddEntities" system
    scene.runSystems(Resources(), listOf(AddTestEntities()))

    scene.defaultSystems.add(Movement())

    return scene
}

class AddTestEntities : System {
    override fun query(): Iterable<KClass<out Component>> {
        return emptySet()
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(0f, 0f)
                ), Shape(
                    arrayOf(
                        Vector2f(0f, 0f), Vector2f(0f, 100f), Vector2f(100f, 100f), Vector2f(100f, 0f)
                    ), Color(1f, 0f, 0f)
                )
            )
        )
    }
}

class Movement : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Transform::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        resources.get<EventQueue>().iterate<MousePositionEvent>().forEach { event ->
            entities.forEach { entity ->
                entity.component<Transform>().get().position = Vector2f(event.position.x.toFloat(), event.position.y.toFloat())
                entity.component<Transform>().mutate()
            }
        }
    }
}