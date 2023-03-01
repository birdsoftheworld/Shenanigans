package shenanigans.game

import org.joml.Vector2f
import org.joml.Vector3f
import shenanigans.engine.ClientEngine
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.emptyEventQueues
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.physics.Collider
import shenanigans.engine.physics.CollisionSystem
import shenanigans.engine.scene.Scene
import shenanigans.engine.util.Transform
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.util.isPointInside
import shenanigans.engine.window.Key
import shenanigans.engine.window.MouseButtonAction
import shenanigans.engine.window.events.KeyboardState
import shenanigans.engine.window.events.MouseButtonEvent
import shenanigans.engine.window.events.MouseState
import shenanigans.game.network.Sendable
import shenanigans.game.player.Player
import shenanigans.game.player.PlayerController
import shenanigans.game.player.PlayerProperties
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
    scene.entities.runSystem(System::executePhysics, AddTestEntities(), ResourcesView(), emptyEventQueues())

    scene.defaultSystems.add(MouseMovementSystem())
    scene.defaultSystems.add(InsertEntitiesOngoing())
    scene.defaultSystems.add(PlayerController())
    scene.defaultSystems.add(CollisionSystem())
    scene.defaultSystems.add(FollowCameraSystem())
//    scene.defaultSystems.add(NetworkSystem())

    return scene
}

class FollowCameraSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Player::class, Transform::class)
    }

    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues,
        entities: EntitiesView,
        lifecycle: EntitiesLifecycle
    ) {
        val first = entities.first()
        val transform = first.component<Transform>().get()
        val camera = resources.get<CameraResource>().camera!!
        camera.reset().translate(
            transform.position.x - camera.screenWidth / 2 + 50,
            transform.position.y - camera.screenHeight / 2 + 50
        )
    }
}

class MousePlayer(var grabbed: Boolean, var dragOffset: Vector2f) : Component {
    fun grab() {
        this.grabbed = true
    }

    fun drop() {
        this.grabbed = false
    }
}

class AddTestEntities : System {
    override fun query(): Iterable<KClass<out Component>> {
        return emptySet()
    }

    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues,
        entities: EntitiesView,
        lifecycle: EntitiesLifecycle
    ) {
        val shape = Shape(
            arrayOf(
                Vector2f(0f, 0f),
                Vector2f(0f, 50f),
                Vector2f(600f, 50f),
                Vector2f(600f, 0f)
            ), Color(1f, 0f, 0f)
        )

        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(0f, 600f)
                ),
                shape,
                Collider(shape, true),
                MousePlayer(false, Vector2f(0f, 0f)),
            )
        )

        val shape2 = Shape(
            arrayOf(
                Vector2f(0f, 0f), Vector2f(0f, 30f), Vector2f(30f, 30f), Vector2f(30f, 0f)
            ), Color(0f, 0f, 1f)
        )
        val sprite = Sprite(TextureManager.createTexture("/playerTexture.png").getRegion(), Vector2f(30f,30f))
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector3f(200f, 500f, 1f),
                ),
                sprite,
                Collider(shape2, false, tracked = true),
                Player(
                    PlayerProperties()
                ),
                Sendable(),
            )
        )

        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector2f(600f, 700f)
                ),
                shape,
                Collider(shape, true, true),
                MousePlayer(false, Vector2f(0f,0f)),
                )
        ))

        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector2f(800f, 600f)
                ),
                shape,
                Collider(shape, true),
                MousePlayer(false, Vector2f(0f,0f)),
                )
        ))
        val shape3 = Shape(
            arrayOf(
                Vector2f(0f, 0f),
                Vector2f(0f, 600f),
                Vector2f(50f, 600f),
                Vector2f(50f, 0f)
            ), Color(0f, 1f, 1f)
        )
        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector3f(400f, 400f, 0.5f)
                ),
                shape3,
                Collider(shape3, true),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        ))
        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector3f(300f, 400f, 0.5f)
                ),
                shape3,
                Collider(shape3, true),
                MousePlayer(false, Vector2f(0f,0f)),
            )
            ))
    }
}


class InsertEntitiesOngoing : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Shape::class,Transform::class)
    }

    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues,
        entities: EntitiesView,
        lifecycle: EntitiesLifecycle
    ) {
        val mousePos = resources.get<MouseState>().position()
        val keyboard = resources.get<KeyboardState>()
        val shape = Shape(
            arrayOf(
                Vector2f(0f, 0f),
                Vector2f(0f, 50f),
                Vector2f(50f, 50f),
                Vector2f(50f, 0f)
            ), Color(.5f, .5f, .5f)
        )
        eventQueues.own.iterate<MouseButtonEvent>().forEach { event ->
            if (keyboard.isPressed(Key.SPACE) && event.action == MouseButtonAction.PRESS) {
                lifecycle.add(
                    sequenceOf(
                        Transform(
                            Vector2f(round((mousePos.x()-25f)/50)*50, round((mousePos.y()-25f)/50)*50)
                        ),
                        shape,
                        Collider(shape, false),
                        MousePlayer(false, Vector2f(0f, 0f)),
                    )
                )
            }
        }
    }
}
class MouseMovementSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(MousePlayer::class, Transform::class)
    }

    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues,
        entities: EntitiesView,
        lifecycle: EntitiesLifecycle
    ) {
        entities.forEach { entity ->
            val mousePlayer = entity.component<MousePlayer>().get()
            if (mousePlayer.grabbed) {
                val transform = entity.component<Transform>().get()
                val position = resources.get<MouseState>().position()
                val transformedPosition = resources.get<CameraResource>().camera!!.untransformPoint(Vector3f(position, 0f))
                transform.position.set(
                    transformedPosition.x() + mousePlayer.dragOffset.x(),
                    transformedPosition.y() + mousePlayer.dragOffset.y(),
                    0f
                )
                entity.component<Transform>().mutate()
            }
        }

        eventQueues.own.iterate<MouseButtonEvent>().forEach { event ->
            entities.forEach { entity ->
                val transform = entity.component<Transform>().get()
                val mousePosition = resources.get<MouseState>().position()
                val transformedPosition =
                    resources.get<CameraResource>().camera!!.untransformPoint(Vector3f(mousePosition, 0f))
                val mousePlayer = entity.component<MousePlayer>().get()
                if (event.action == MouseButtonAction.PRESS && entity.component<Shape>().get()
                        .isPointInside(Vector2f(transformedPosition.x, transformedPosition.y), transform)
                ) {
                    mousePlayer.dragOffset.x = transform.position.x - transformedPosition.x()
                    mousePlayer.dragOffset.y = transform.position.y - transformedPosition.y()
                    mousePlayer.grab()
                }
                if (event.action == MouseButtonAction.RELEASE) {
                    mousePlayer.drop()
                    transform.position.x = round(transform.position.x / 50) * 50
                    transform.position.y = round(transform.position.y / 50) * 50
                }
            }
        }
    }
}
