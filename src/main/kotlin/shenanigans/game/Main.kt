package shenanigans.game

import org.joml.Vector2f
import shenanigans.engine.ClientEngine
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.physics.Collider
import shenanigans.engine.physics.CollisionSystem
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.scene.Scene
import shenanigans.engine.util.Transform
import shenanigans.engine.util.isPointInside
import shenanigans.engine.window.Key
import shenanigans.engine.window.MouseButtonAction
import shenanigans.engine.window.events.KeyboardState
import shenanigans.engine.window.events.MouseButtonEvent
import shenanigans.engine.window.events.MouseState
import shenanigans.game.network.Sendable
import kotlin.math.round
import kotlin.reflect.KClass

fun main() {
    ClientEngine(testScene()).run()
}

fun testScene(): Scene {
    val scene = Scene()

    // NOTE: in the future, this will not be the recommended way to populate a scene
    //       instead, the engine will have a facility for running systems once
    //       which will be used with a canonical "AddEntities" system
    scene.runSystems(ResourcesView(), listOf(AddTestEntities()))

    scene.defaultSystems.add(MouseMovementSystem())
    scene.defaultSystems.add(KeyboardMovementSystem())
    scene.defaultSystems.add(CollisionSystem())
    scene.defaultSystems.add(FollowCameraSystem())
//    scene.defaultSystems.add(NetworkSystem())

    return scene
}

class FollowCameraSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(KeyboardPlayer::class, Transform::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val first = entities.first()
        val transform = first.component<Transform>().get()
        val camera = resources.get<CameraResource>().camera!!
        camera.reset().translate(transform.position.x - camera.screenWidth / 2 + 50, transform.position.y - camera.screenHeight / 2 + 50)
    }
}

class MousePlayer(var grabbed : Boolean, var dragOffset : Vector2f) : Component{
    fun grab(){this.grabbed=true}
    fun drop(){this.grabbed=false}
}
data class KeyboardPlayer(val speed: Float) : Component

class AddTestEntities : System {
    override fun query(): Iterable<KClass<out Component>> {
        return emptySet()
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val shape = Shape(
            arrayOf(
                Vector2f(0f, 0f), Vector2f(0f, 50f), Vector2f(50f, 50f), Vector2f(50f, 0f)
            ), Color(1f, 0f, 0f)
        )

        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(0f, 0f)
                ),
                shape,
                Collider(shape, true),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )

        val shape2 = Shape(
            arrayOf(
                Vector2f(0f, 0f), Vector2f(0f, 100f), Vector2f(100f, 100f), Vector2f(100f, 0f)
            ), Color(0f, 0f, 1f)
        )
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(200f, 200f)
                ),
                shape2,
                Collider(shape2, false),
                KeyboardPlayer(500f),
                Sendable(),
            )
        )

        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(200f, 200f)
                ),
                shape,
                Collider(shape, false)
            )
        )
    }
}

class MouseMovementSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(MousePlayer::class, Transform::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        entities.forEach { entity ->
            val mousePlayer = entity.component<MousePlayer>().get()
            if(mousePlayer.grabbed){
                val transform = entity.component<Transform>().get()
                val position = resources.get<MouseState>().position()
                val transformedPosition = resources.get<CameraResource>().camera!!.untransformPoint(Vector2f(position))
                transform.position.set(transformedPosition.x() + mousePlayer.dragOffset.x(), transformedPosition.y() + mousePlayer.dragOffset.y())
                entity.component<Transform>().mutate()
            }
        }

        resources.get<EventQueue>().iterate<MouseButtonEvent>().forEach { event ->
            entities.forEach { entity ->
                val transform = entity.component<Transform>().get()
                val mousePosition = resources.get<MouseState>().position()
                val transformedPosition = resources.get<CameraResource>().camera!!.untransformPoint(Vector2f(mousePosition))
                val mousePlayer = entity.component<MousePlayer>().get()
                if(event.action == MouseButtonAction.PRESS && entity.component<Shape>().get().isPointInside(transformedPosition, transform)){
                    mousePlayer.dragOffset.x = transform.position.x - transformedPosition.x()
                    mousePlayer.dragOffset.y = transform.position.y - transformedPosition.y()
                    mousePlayer.grab()
                }
                if(event.action == MouseButtonAction.RELEASE){
                    mousePlayer.drop()
                    transform.position.x = round(transform.position.x/50)*50
                    transform.position.y = round(transform.position.y/50)*50
                }
            }
        }
    }
}

class KeyboardMovementSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(KeyboardPlayer::class, Transform::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
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