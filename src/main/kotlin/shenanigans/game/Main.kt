package shenanigans.game

import org.joml.Vector2f
import org.joml.Vector3f
import shenanigans.engine.ClientEngine
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.net.Client
import shenanigans.engine.net.ClientOnly
import shenanigans.engine.net.Network
import shenanigans.engine.net.SendableClass
import shenanigans.engine.physics.Collider
import shenanigans.engine.physics.CollisionSystem
import shenanigans.engine.scene.Scene
import shenanigans.engine.util.Transform
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.util.isPointInside
import shenanigans.engine.util.shapes.Rectangle
import shenanigans.engine.window.Key
import shenanigans.engine.window.MouseButtonAction
import shenanigans.engine.window.events.KeyboardState
import shenanigans.engine.window.events.MouseButtonEvent
import shenanigans.engine.window.events.MouseState
import shenanigans.game.network.*
import shenanigans.game.player.Player
import shenanigans.game.player.PlayerController
import shenanigans.game.player.PlayerProperties
import java.util.*
import kotlin.math.round
import kotlin.reflect.KClass

fun main() {
    val engine = ClientEngine(testScene(), Network(Client(), sendables()))

    engine.runPhysicsOnce(AddTestEntities())

    engine.run()
}

fun testScene(): Scene {
    val scene = Scene()

    scene.defaultSystems.add(MouseMovementSystem())
    scene.defaultSystems.add(InsertEntitiesOngoing())
    scene.defaultSystems.add(PlayerController())
    scene.defaultSystems.add(CollisionSystem())
    scene.defaultSystems.add(FollowCameraSystem())
    scene.defaultSystems.add(NetworkSystem())

    return scene
}

@ClientOnly
class Followed(val func: () -> Vector3f) : Component

class FollowCameraSystem : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val first = query(setOf(Followed::class)).first()
        val camera = resources.get<CameraResource>().camera!!
        val pos = first.component<Followed>().get().func()
        camera.reset().translate(
            pos.x - camera.screenWidth / 2,
            pos.y - camera.screenHeight / 2
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
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val polygon = Rectangle(600f, 50f)

        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector3f(0f, 600f, 0.5f)
                ),
                Shape(polygon, Color(1f, 0f, 0f)),
                Collider(polygon, true),
                MousePlayer(false, Vector2f(0f, 0f)),
            )
        )

        val playerShape = PlayerController.SHAPE_BASE
        val sprite = Sprite(TextureManager.createTexture(TextureKey("player"), "/playerTexture.png").getRegion(), playerShape)
        val player = Player(
            PlayerProperties()
        )
        val playerTransform = Transform(
            Vector3f(200f, 500f, 0.5f),
        )
        lifecycle.add(
            sequenceOf(
                playerTransform,
                sprite,
                Collider(playerShape, false, tracked = true),
                player,
                Followed {
                    val p = Vector3f(playerTransform.position)
                    p.x += PlayerController.SHAPE_BASE.width / 2
                    p.y += PlayerController.SHAPE_BASE.height / 2
                    if (player.crouching) {
                        p.y -= PlayerController.SHAPE_BASE.height - PlayerController.SHAPE_CROUCHED.height
                    }
                    p
                },
                Synchronized()
            )
        )

        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector3f(600f, 700f, 0.5f)
                ),
                Shape(polygon, Color(1f, 0f, 0f)),
                Collider(polygon, true),
                MousePlayer(false, Vector2f(0f,0f)),
                )
        ))

        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector3f(800f, 600f, 0.5f)
                ),
                Shape(polygon, Color(1f, 0f, 0f)),
                Collider(polygon, true),
                MousePlayer(false, Vector2f(0f,0f)),
                )
        ))
        val polygon3 = Rectangle(50f, 600f)
        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector3f(300f, 400f, 0.5f)
                ),
                Shape(polygon3, Color(0f, 1f, 1f)),
                Collider(polygon3, true),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        ))
    }
}


class InsertEntitiesOngoing : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val mousePos = resources.get<MouseState>().position()
        val keyboard = resources.get<KeyboardState>()
        val shape = Shape(
            Rectangle(50f, 50f),
            Color(.5f, .5f, .5f)
        )
        eventQueues.own.receive(MouseButtonEvent::class).forEach { event ->
            if (keyboard.isPressed(Key.SPACE) && event.action == MouseButtonAction.PRESS) {
                lifecycle.add(
                    sequenceOf(
                        Transform(
                            Vector2f(round((mousePos.x()-25f)/50)*50, round((mousePos.y()-25f)/50)*50)
                        ),
                        shape,
                        Collider(shape.polygon, false),
                        MousePlayer(false, Vector2f(0f, 0f)),
                    )
                )
            }
        }
    }
}
class MouseMovementSystem : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(setOf(MousePlayer::class, Transform::class))

        entities.forEach { entity ->
            val mousePlayer = entity.component<MousePlayer>().get()
            if (mousePlayer.grabbed) {
                val transform = entity.component<Transform>().get()
                val position = resources.get<MouseState>().position()
                val transformedPosition = resources.get<CameraResource>().camera!!.untransformPoint(Vector3f(position, 0f))
                transform.position.set(
                    transformedPosition.x() + mousePlayer.dragOffset.x(),
                    transformedPosition.y() + mousePlayer.dragOffset.y(),
                    transform.position.z
                )
                entity.component<Transform>().mutate()
            }
        }

        eventQueues.own.receive(MouseButtonEvent::class).forEach { event ->
            entities.forEach { entity ->
                val transform = entity.component<Transform>().get()
                val mousePosition = resources.get<MouseState>().position()
                val transformedPosition =
                    resources.get<CameraResource>().camera!!.untransformPoint(Vector3f(mousePosition, 0f))
                val mousePlayer = entity.component<MousePlayer>().get()
                if (event.action == MouseButtonAction.PRESS && entity.component<Shape>().get()
                        .polygon.isPointInside(Vector2f(transformedPosition.x, transformedPosition.y), transform)
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
