package shenanigans.game

import org.joml.Vector2f
import org.joml.Vector3f
import shenanigans.engine.ClientEngine
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.events.eventQueues
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.physics.Collider
import shenanigans.engine.physics.CollisionSystem
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.scene.Scene
import shenanigans.engine.util.Transform
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.util.isPointInside
import shenanigans.engine.util.shapes.Rectangle
import shenanigans.engine.window.Key
import shenanigans.engine.window.MouseButton
import shenanigans.engine.window.MouseButtonAction
import shenanigans.engine.window.events.KeyboardState
import shenanigans.engine.window.events.MouseButtonEvent
import shenanigans.engine.window.events.MouseState
import shenanigans.game.network.NetworkSystem
import shenanigans.game.network.Synchronized
import shenanigans.engine.window.events.*
import shenanigans.game.Blocks.*
import shenanigans.game.player.Player
import shenanigans.game.player.PlayerController
import shenanigans.game.player.PlayerProperties
import kotlin.math.round
import kotlin.reflect.KClass

fun main() {
    val engine = ClientEngine(testScene())

    engine.runPhysicsOnce(BuildLevelSystem())

    engine.run()
}

fun testScene(): Scene {
    val scene = Scene()

    scene.defaultSystems.add(InsertNewEntitiesSystem())
    scene.defaultSystems.add(OscillatingBlocksSystem())
    scene.defaultSystems.add(MouseMovementSystem())
    scene.defaultSystems.add(PlayerController())
    scene.defaultSystems.add(CollisionSystem())
    scene.defaultSystems.add(FollowCameraSystem())
    scene.defaultSystems.add(NetworkSystem())

    return scene
}

class FollowCameraSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Player::class, Transform::class)
    }

    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        entities: EntitiesView,
        lifecycle: EntitiesLifecycle
    ) {
        val first = entities.first()
        val transform = first.component<Transform>().get()
        val camera = resources.get<CameraResource>().camera!!
        camera.reset().translate(
            transform.position.x - camera.screenWidth / 2 + 20 ,
            transform.position.y - camera.screenHeight / 2 + 20
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
data class KeyboardPlayer(val speed: Float) : Component

//class AddTestEntities : System {
//    override fun query(): Iterable<KClass<out Component>> {
//        return emptySet()
//    }
//
//    override fun executePhysics(
//        resources: ResourcesView,
//        eventQueues: EventQueues<LocalEventQueue>,
//        entities: EntitiesView,
//        lifecycle: EntitiesLifecycle
//    ) {
//        val polygon = Rectangle(600f, 50f)
//
//        lifecycle.add(
//            sequenceOf(
//                Transform(
//                    Vector3f(0f, 600f, 0.5f)
//                ),
//                Shape(polygon, Color(1f, 0f, 0f)),
//                Collider(polygon, true),
//                MousePlayer(false, Vector2f(0f, 0f)),
//            )
//        )
//
//        val playerShape = PlayerController.SHAPE_BASE
//        val sprite = Sprite(TextureManager.createTexture("/playerTexture.png").getRegion(), playerShape)
//        lifecycle.add(
//            sequenceOf(
//                Transform(
//                    Vector3f(200f, 500f, 0.5f),
//                ),
//                sprite,
//                Collider(playerShape, false, tracked = true),
//                Player(
//                    PlayerProperties()
//                ),
//                Synchronized()
//            )
//        )
//
//        lifecycle.add((
//            sequenceOf(
//                Transform(
//                    Vector3f(600f, 700f, 0.5f)
//                ),
//                Shape(polygon, Color(1f, 0f, 0f)),
//                Collider(polygon, true),
//                MousePlayer(false, Vector2f(0f,0f)),
//                )
//        ))
//
//        lifecycle.add((
//            sequenceOf(
//                Transform(
//                    Vector3f(800f, 600f, 0.5f)
//                ),
//                Shape(polygon, Color(1f, 0f, 0f)),
//                Collider(polygon, true),
//                MousePlayer(false, Vector2f(0f,0f)),
//                )
//        ))
//        val polygon3 = Rectangle(50f, 600f)
//        lifecycle.add((
//            sequenceOf(
//                Transform(
//                    Vector3f(300f, 400f, 0.5f)
//                ),
//                Shape(polygon3, Color(0f, 1f, 1f)),
//                Collider(polygon3, true),
//                MousePlayer(false, Vector2f(0f,0f)),
//            )
//        ))
//    }
//}


//class InsertEntitiesOngoing : System {
//    override fun query(): Iterable<KClass<out Component>> {
//        return setOf(Shape::class,Transform::class)
//    }
//
//    override fun executePhysics(
//        resources: ResourcesView,
//        eventQueues: EventQueues<LocalEventQueue>,
//        entities: EntitiesView,
//        lifecycle: EntitiesLifecycle
//    ) {
//        val mousePos = resources.get<MouseState>().position()
//        val keyboard = resources.get<KeyboardState>()
//        val shape = Shape(
//            Rectangle(50f, 50f),
//            Color(.5f, .5f, .5f)
//        )
//        eventQueues.own.receive(MouseButtonEvent::class).forEach { event ->
//            if (keyboard.isPressed(Key.SPACE) && event.action == MouseButtonAction.PRESS) {
//                lifecycle.add(
//                    sequenceOf(
//                        Transform(
//                            Vector2f(round((mousePos.x()-25f)/50)*50, round((mousePos.y()-25f)/50)*50)
//                        ),
//                        shape,
//                        Collider(shape.polygon, false),
//                        MousePlayer(false, Vector2f(0f, 0f)),
//                    )
//                )
//            }
//        }
//    }
//}
class MouseMovementSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(MousePlayer::class, Transform::class)
    }

    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        entities: EntitiesView,
        lifecycle: EntitiesLifecycle
    ) {
        entities.forEach { entity ->
            val mousePlayer = entity.component<MousePlayer>().get()
            if (mousePlayer.grabbed) {
                val transform = entity.component<Transform>().get()
                val position = resources.get<MouseState>().position()
                val transformedPosition = resources.get<CameraResource>().camera!!.untransformPoint(Vector3f(position, 0f))
                val dragOffset = mousePlayer.dragOffset
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

                transform.position.set(transformedPosition.x() + mousePlayer.dragOffset.x(), transformedPosition.y() + mousePlayer.dragOffset.y(),transform.position.z)
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
                if (entity.component<Collider>().get().polygon.isPointInside(Vector2f(transformedPosition.x, transformedPosition.y), transform)
                ) {
                    if(resources.get<KeyboardState>().isPressed(Key.Q)){
                        lifecycle.del(entity.id)
                    }
                    if(event.action == MouseButtonAction.PRESS){

                        mousePlayer.dragOffset.x = transform.position.x - transformedPosition.x()
                        mousePlayer.dragOffset.y = transform.position.y - transformedPosition.y()
                        mousePlayer.grab()
                    }
                }
                if (entity.component<MousePlayer>().get().grabbed) {
                    if(event.action == MouseButtonAction.RELEASE) {
                        mousePlayer.drop()
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
