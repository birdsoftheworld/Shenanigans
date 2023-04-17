package shenanigans.game

import org.joml.Vector2f
import org.joml.Vector3f
import shenanigans.engine.ClientEngine
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.net.Client
import shenanigans.engine.net.ClientOnly
import shenanigans.engine.net.Network
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
import shenanigans.engine.window.events.MouseScrollEvent
import shenanigans.engine.window.events.MouseState
import shenanigans.game.blocks.BuildLevelSystem
import shenanigans.game.blocks.InsertNewEntitiesSystem
import shenanigans.game.blocks.OscillatingBlock
import shenanigans.game.blocks.OscillatingBlocksSystem
import shenanigans.game.network.ClientSystem
import shenanigans.game.network.sendables
import shenanigans.game.player.PlayerController
import kotlin.math.round
import kotlin.reflect.KClass

fun main() {
    val engine = ClientEngine(testScene(), Network(Client(), sendables()))

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
    scene.defaultSystems.add(ClientSystem())

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

@ClientOnly
class MousePlayer(var grabbed: Boolean, var dragOffset: Vector2f) : Component {
    fun grab() {
        this.grabbed = true
    }

    fun drop() {
        this.grabbed = false
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
