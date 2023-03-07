package shenanigans.game

import org.joml.Vector2f
import shenanigans.engine.ClientEngine
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.physics.Collider
import shenanigans.engine.physics.CollisionSystem
import shenanigans.engine.scene.Scene
import shenanigans.engine.util.Transform
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.util.isPointInside
import shenanigans.engine.window.MouseButtonAction
import shenanigans.engine.window.events.*
import shenanigans.game.Blocks.*
import shenanigans.game.player.Player
import shenanigans.game.player.PlayerController
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
    scene.runSystems(ResourcesView(), listOf(BuildLevelSystem()))

    scene.defaultSystems.add(MouseMovementSystem())
    scene.defaultSystems.add(InsertNewEntitiesSystem())
    scene.defaultSystems.add(OscillatingBlocksSystem())
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
enum class direction(sign : Int) {
    Up(0),Right(1),Down(2),Left(3)
}


data class KeyboardPlayer(val speed: Float) : Component

fun newShape(h : Float, w : Float, color : Color) : Shape{
    return Shape(
        arrayOf(
            Vector2f(0f, 0f),
            Vector2f(0f, h),
            Vector2f(w, h),
            Vector2f(w, 0f)
        ), color
    )
}

fun newShape(h : Float, w : Float) : Shape{
    return Shape(
        arrayOf(
            Vector2f(0f, 0f),
            Vector2f(0f, h),
            Vector2f(w, h),
            Vector2f(w, 0f)
        ), Color(.5f,.5f,.5f)
    )
}




class MouseMovementSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(MousePlayer::class, Transform::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        entities.forEach { entity ->
            val mousePlayer = entity.component<MousePlayer>().get()
            val eventQueue = resources.get<EventQueue>()
            if(mousePlayer.grabbed){
                val transform = entity.component<Transform>().get()
                val position = resources.get<MouseState>().position()
                val transformedPosition = resources.get<CameraResource>().camera!!.untransformPoint(Vector2f(position))
                val dragOffset = mousePlayer.dragOffset
                val x = dragOffset.x
                val y = dragOffset.y
                var scroll: Float
                eventQueue.iterate<MouseScrollEvent>().forEach{ event ->
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

                transform.position.set(transformedPosition.x() + mousePlayer.dragOffset.x(), transformedPosition.y() + mousePlayer.dragOffset.y())
                entity.component<Transform>().mutate()

            }
        }

        resources.get<EventQueue>().iterate<MouseButtonEvent>().forEach { event ->
            entities.forEach { entity ->
                val transform = entity.component<Transform>().get()
                val mousePosition = resources.get<MouseState>().position()
                val transformedPosition =
                    resources.get<CameraResource>().camera!!.untransformPoint(Vector2f(mousePosition))
                val mousePlayer = entity.component<MousePlayer>().get()
                if(entity.componentOpt<Collider>() != null) {
                    if (event.action == MouseButtonAction.PRESS && entity.component<Collider>().get().isPointInside(transformedPosition, transform)
                    ) {
                        mousePlayer.dragOffset.x = transform.position.x - transformedPosition.x()
                        mousePlayer.dragOffset.y = transform.position.y - transformedPosition.y()
                        mousePlayer.grab()
                    }
                }
                if (event.action == MouseButtonAction.RELEASE && entity.component<MousePlayer>().get().grabbed) {
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
