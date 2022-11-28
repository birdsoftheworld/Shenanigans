package shenanigans.game

import org.joml.Vector2f
import shenanigans.engine.Engine
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.physics.Collider
import shenanigans.engine.physics.CollisionSystem
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.scene.Scene
import shenanigans.engine.ui.Button
import shenanigans.engine.ui.ButtonSystem
import shenanigans.engine.util.Transform
import shenanigans.engine.window.Key
import shenanigans.engine.window.events.KeyboardState
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

    scene.defaultSystems.add(MouseMovementSystem())
    scene.defaultSystems.add(KeyboardMovementSystem())
    scene.defaultSystems.add(CollisionSystem())
    scene.defaultSystems.add(ButtonSystem())

    return scene
}

class MousePlayer : Component
data class KeyboardPlayer(val speed: Float) : Component

class AddTestEntities : System {
    override fun query(): Iterable<KClass<out Component>> {
        return emptySet()
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val shape = Shape(
            arrayOf(
                Vector2f(0f, 0f), Vector2f(0f, 100f), Vector2f(100f, 100f), Vector2f(100f, 0f)
            ), Color(1f, 0f, 0f)
        )

        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(0f, 0f)
                ),
                shape,
                Collider(shape, true),
//                MousePlayer()
            )
        )

        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(200f, 200f)
                ),
                Shape(
                    arrayOf(
                        Vector2f(0f, 0f), Vector2f(0f, 100f), Vector2f(100f, 100f), Vector2f(100f, 0f)
                    ), Color(0f, 0f, 1f)
                ),
                Collider(shape, false),
                KeyboardPlayer(500f),
            )
        )

        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(200f, 200f)
                ),
                shape,
                Button(),
                Collider(shape, false)
            )
        )
    }
}

class MouseMovementSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(MousePlayer::class, Transform::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        resources.get<EventQueue>().iterate<MousePositionEvent>().forEach { event ->
            entities.forEach { entity ->
                entity.component<Transform>().get().position.set(event.position)
                entity.component<Transform>().mutate()
            }
        }
    }
}

class KeyboardMovementSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(KeyboardPlayer::class, Transform::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val keyboard = resources.get<KeyboardState>()
        val deltaTime = resources.get<DeltaTime>().deltaTime

        entities.forEach { entity ->
            val velocity = Vector2f()
            if (keyboard.isPressed(Key.W)) {
                velocity.add(Vector2f(0f, -1f))
            }
            if (keyboard.isPressed(Key.A)) {
                velocity.add(Vector2f(-1f, 0f))
            }
            if (keyboard.isPressed(Key.S)) {
                velocity.add(Vector2f(0f, 1f))
            }
            if (keyboard.isPressed(Key.D)) {
                velocity.add(Vector2f(1f, 0f))
            }

            if (velocity.length() > 0) {
                velocity.normalize((entity.component<KeyboardPlayer>().get().speed * deltaTime).toFloat())

                val transform = entity.component<Transform>()
                transform.get().position.add(velocity)
                transform.mutate()
            }
        }
    }
}