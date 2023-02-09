package shenanigans.game

import org.joml.Vector2f
import shenanigans.engine.ClientEngine
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
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
import shenanigans.engine.window.KeyAction
import shenanigans.engine.window.MouseButtonAction
import shenanigans.engine.window.events.*
import shenanigans.game.network.Sendable
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
    scene.runSystems(ResourcesView(), listOf(AddTestEntities()))

    scene.defaultSystems.add(MouseMovementSystem())
    scene.defaultSystems.add(OscillatingBlocksSystem())
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
class SpawnPoint() : Component
class ScaryBlock() : Component
class StickyBlock() : Component
class SlipperyBlock() : Component

class OscillatingPlayer(val distanceToOscillate : Float, var horizontal : Boolean, var startPos : Vector2f, var speed : Float) : Component{
    fun changeAxis(){
        this.horizontal = !horizontal
        println(this.horizontal)
    }

    fun changeDirection(){
        this.speed*=-1
    }

    fun setPos(newPos : Vector2f){
        this.startPos = newPos
    }

}
data class KeyboardPlayer(val speed: Float) : Component

fun newShape(h : Float, w : Float, color : Triple<Float,Float,Float>) : Shape{
    return Shape(
        arrayOf(
            Vector2f(0f, 0f),
            Vector2f(0f, h),
            Vector2f(w, h),
            Vector2f(w, 0f)
        ), Color(color.first,color.second,color.third)
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
class AddTestEntities : System {
    override fun query(): Iterable<KClass<out Component>> {
        return emptySet()
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        //Shapes Declaration
        val floorShape = Shape(
            arrayOf(
                Vector2f(0f, 0f),
                Vector2f(0f, 50f),
                Vector2f(600f, 50f),
                Vector2f(600f, 0f)
            ), Color(1f, 0f, 0f)
        )

        val playerShape = Shape(
            arrayOf(
                Vector2f(0f, 0f), Vector2f(0f, 30f), Vector2f(30f, 30f), Vector2f(30f, 0f)
            ), Color(0f, 0f, 1f)
        )

        val wallShape = Shape(
            arrayOf(
                Vector2f(0f, 0f),
                Vector2f(0f, 600f),
                Vector2f(50f, 600f),
                Vector2f(50f, 0f)
            ), Color(0f, 1f, 1f)
        )

        val oscillatingShape = Shape(
            arrayOf(
                Vector2f(0f, 0f),
                Vector2f(0f, 50f),
                Vector2f(50f, 50f),
                Vector2f(50f, 0f)
            ), Color(.5f, .5f, .5f)
        )

        val scaryShape = newShape(25f,25f)

        val stickyShape = newShape(5f,50f, Triple(.2f,.2f,.2f))

        val slipperyShape = newShape(5f,50f, Triple(7f,.7f,.7f))

        //Sprites Declaration
        val playerSprite = Sprite(TextureManager.createTexture("/playerTexture.png").getRegion(), Vector2f(30f,30f))
        val respawnSprite = Sprite(TextureManager.createTexture("/sprite.png").getRegion(), Vector2f(30f,30f))

        //Oscillating Block
        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector2f(100f, 500f)
                ),
                oscillatingShape,
                Collider(oscillatingShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
                OscillatingPlayer(100f,true, Vector2f(100f, 500f), .01f),
            )
        ))


        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(0f, 600f)
                ),
                floorShape,
                Collider(floorShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )



        //Player Respawn Block
        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector2f(100f, 500f)
                ),
                playerShape,
                MousePlayer(false, Vector2f(0f,0f)),
                SpawnPoint(),
            )
        ))


        //PLAYER
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(100f, 500f),
                ),
                playerSprite,
                Collider(playerShape, false),
                Player(500f),
                Sendable(),
            )
        )



        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector2f(600f, 700f)
                ),
                floorShape,
                Collider(floorShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        ))

        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector2f(800f, 600f)
                ),
                floorShape,
                Collider(floorShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
                )
        ))


        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector2f(400f, 400f)
                ),
                wallShape,
                Collider(wallShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        ))



    }
}

class InsertEntitiesOngoing : System{
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Shape::class,Transform::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val mousePos = resources.get<MouseState>().position()
        val keyboard = resources.get<KeyboardState>()
        val ShapeI = Shape(
            arrayOf(
                Vector2f(0f, 0f),
                Vector2f(0f, 50f),
                Vector2f(50f, 50f),
                Vector2f(50f, 0f)
            ), Color(.5f, .5f, .5f)
        )
        resources.get<EventQueue>().iterate<MouseButtonEvent>().forEach { event ->
            if (keyboard.isPressed(Key.SPACE) && event.action == MouseButtonAction.PRESS) {
                lifecycle.add(
                    sequenceOf(
                        Transform(
                            Vector2f(round((mousePos.x()-25f)/50)*50, round((mousePos.y()-25f)/50)*50)
                        ),
                        ShapeI,
                        Collider(ShapeI, false),
                        MousePlayer(false, Vector2f(0f, 0f)),
                    )
                )
            }
        }
    }
}
class OscillatingBlocksSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(OscillatingPlayer::class, Transform::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        entities.forEach { entity ->
            val transform = entity.component<Transform>().get()
            val oscillatingPlayer = entity.component<OscillatingPlayer>().get()
            val mousePlayer = entity.component<MousePlayer>().get()
            if(!entity.component<MousePlayer>().get().grabbed){
                when(oscillatingPlayer.horizontal){
                    true -> entity.component<Transform>().get().position.x += oscillatingPlayer.speed

                    false -> entity.component<Transform>().get().position.y += oscillatingPlayer.speed

                }
            }
            when(oscillatingPlayer.horizontal){
                true -> if(!mousePlayer.grabbed && Math.abs(transform.position.x - oscillatingPlayer.startPos.x) > oscillatingPlayer.distanceToOscillate){
                    oscillatingPlayer.changeDirection()
                }

                false -> if(!mousePlayer.grabbed && Math.abs(transform.position.y - oscillatingPlayer.startPos.y) > oscillatingPlayer.distanceToOscillate){
                    oscillatingPlayer.changeDirection()
                }

            }

        }
    }

}

class MouseMovementSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(MousePlayer::class, Transform::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        entities.forEach { entity ->
            val mousePlayer = entity.component<MousePlayer>().get()
            val EventQueue = resources.get<EventQueue>()
            if(mousePlayer.grabbed){
                val transform = entity.component<Transform>().get()
                val mousePos = resources.get<MouseState>().position()
                val position = resources.get<MouseState>().position()
                val transformedPosition = resources.get<CameraResource>().camera!!.untransformPoint(Vector2f(position))
                val dragOffset = mousePlayer.dragOffset
                val x = dragOffset.x
                val y = dragOffset.y
                EventQueue.iterate<KeyEvent>().forEach{event ->
                    if(event.key == Key.Q && event.action == KeyAction.PRESS){
                        transform.rotation -= (Math.PI/2).toFloat()
                        dragOffset.x = y
                        dragOffset.y = -x
                        if(entity.componentOpt<OscillatingPlayer>() != null){
                            entity.component<OscillatingPlayer>().get().changeAxis()
                        }
                    }
                    if(event.key == Key.E && event.action == KeyAction.PRESS){
                        transform.rotation += (Math.PI/2).toFloat()
                        dragOffset.x = -y
                        dragOffset.y = x
                        if(entity.componentOpt<OscillatingPlayer>() != null){
                            entity.component<OscillatingPlayer>().get().changeAxis()
                        }
                    }
                }

                transform.position.set(transformedPosition.x() + mousePlayer.dragOffset.x(), transformedPosition.y() + mousePlayer.dragOffset.y())
                entity.component<Transform>().mutate()

            }
        }


        resources.get<EventQueue>().iterate<MouseScrollEvent>().forEach { event ->
            val scroll = MouseScrollEvent
            if(true){
                println(scroll)
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
                    if (entity.componentOpt<OscillatingPlayer>() != null){
                        entity.component<OscillatingPlayer>().get().setPos(transform.position)
                    }
                }
            }
        }
    }
}
