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
import shenanigans.game.player.PlayerProperties
import java.security.cert.TrustAnchor
import kotlin.math.abs
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
class SpringBlock() : Component
enum class direction(sign : Int) {
    Up(0),Right(1),Down(2),Left(3)
}
class OscillatingBlock(val distanceToOscillate : Float, var startPos : Vector2f, var speed : Float, var dir : direction = direction.Right) : Component{

     fun rotate(clockwise : Boolean){
         if(clockwise){
             when(dir){
                 direction.Up -> dir = direction.Right
                 direction.Right -> dir = direction.Down
                 direction.Down -> dir = direction.Left
                 direction.Left -> dir = direction.Up
             }
         }
         else{
             when(dir){
                 direction.Down -> dir = direction.Right
                 direction.Left -> dir = direction.Down
                 direction.Up -> dir = direction.Left
                 direction.Right -> dir = direction.Up
             }
         }
         println(dir)
     }

    fun reset(){
        speed = abs(speed)
    }
    fun changeDirection(){
        speed*=-1
    }
    fun newStartPos(x : Float, y : Float){
        this.startPos.set(x,y)
    }
}

data class TeleporterBlock(val num : Int) : Component{
    var targetPos = Vector2f(0f,0f)
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

        val scaryShape = newShape(50f,50f)

        val springShape = newShape(50f,50f)

        val stickyShape = newShape(4f,50f, Color(0.56666666666f, 0.60833333333f,0.25555555555f))

        val slipperyShape = newShape(5f,50f, Color(0.38333333333f,.62222222222f,.65833333333f))

        val teleportShape =newShape(25f,25f)

        //Sprites Declaration
        val playerSprite = Sprite(TextureManager.createTexture("/playerTexture.png").getRegion(), Vector2f(30f,30f))
        val respawnSprite = Sprite(TextureManager.createTexture("/sprite.png").getRegion(), Vector2f(30f,30f))
        val oscillatingSprite = Sprite(TextureManager.createTexture("/betterArrow.png").getRegion(),Vector2f(50f,50f))
        val springSprite = Sprite(TextureManager.createTexture("/spring.png").getRegion(),Vector2f(50f,50f))
        val scarySprite = Sprite(TextureManager.createTexture("/hole.png").getRegion(), Vector2f(50f,50f))
        val teleportarASprite = Sprite(TextureManager.createTexture("/teleporterA.png").getRegion(), Vector2f(25f,25f))
        val teleportarBSprite = Sprite(TextureManager.createTexture("/teleporterB.png").getRegion(), Vector2f(25f,25f))


        //Oscillating Block
        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector2f(100f, 500f)
                ),
                oscillatingSprite,
                Collider(oscillatingShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
                OscillatingBlock(50f, Vector2f(100f, 500f), .01f),
            )
        ))
        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector2f(50f, 500f)
                ),
                teleportarASprite,
                Collider(teleportShape, true, false, tracked = true),
                MousePlayer(false, Vector2f(0f,0f)),
                TeleporterBlock(0),
            )
        ))

        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector2f(600f, 500f)
                ),
                teleportarBSprite,
                Collider(teleportShape, true, false, tracked = true),
                MousePlayer(false, Vector2f(0f,0f)),
                TeleporterBlock(1),
            )
        ))


        //scaryBlock
        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector2f(100f, 500f)
                ),
                scarySprite,
                Collider(scaryShape, true, false, tracked = true),
                MousePlayer(false, Vector2f(0f,0f)),
                ScaryBlock(),
            )
        ))

        //springBlock
        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector2f(300f, 700f)
                ),
                springSprite,
                Collider(springShape, true, false, tracked = true),
                MousePlayer(false, Vector2f(0f,0f)),
                SpringBlock(),
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

        //slipppery block
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(200f, 600f)
                ),
                slipperyShape,
                SlipperyBlock(),
                Collider(slipperyShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(250f, 600f)
                ),
                slipperyShape,
                SlipperyBlock(),
                Collider(slipperyShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(300f, 600f)
                ),
                slipperyShape,
                SlipperyBlock(),
                Collider(slipperyShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )
        //sticky block
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(0f, 600f)
                ),
                stickyShape,
                StickyBlock(),
                Collider(stickyShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )

        //sticky block
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(0f, 600f)
                ),
                stickyShape,
                StickyBlock(),
                Collider(stickyShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )

        val nullShape = Shape(
            arrayOf(
                Vector2f(0f, 0f),
                Vector2f(0f, 30f),
                Vector2f(30f, 30f),
                Vector2f(30f, 0f)
            ), Color(.5f, .5f, .5f)
        )
        //Player Respawn Block
        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector2f(100f, 500f)
                ),
                playerShape,
                MousePlayer(false, Vector2f(0f,0f)),
                Collider(nullShape, true, true),
                SpawnPoint(),
            )
        ))


        //PLAYER
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(200f, 500f),
                ),
                playerSprite,
                Collider(playerShape, false, tracked = true),
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
class TeleporterSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(TeleporterBlock::class, Transform::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        entities.forEach { entity ->
            val tpBlock = entity.component<TeleporterBlock>()
            if(tpBlock.get().num == 0) {
                entities.forEach { entity2 ->
                    if (entity2.component<TeleporterBlock>().get().num == 1) {
                        tpBlock.get().targetPos.set(entity2.component<Transform>().get().position)
                    }
                }
            }
        }
    }
}
class OscillatingBlocksSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(OscillatingBlock::class, Transform::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        entities.forEach { entity ->
            val pos = entity.component<Transform>().get().position
            val oscillatingBlock = entity.component<OscillatingBlock>().get()
            val mousePlayer = entity.component<MousePlayer>().get()
//            println(oscillatingBlock.speed)
            if(!mousePlayer.grabbed){
                if(abs(pos.x - oscillatingBlock.startPos.x) > oscillatingBlock.distanceToOscillate || abs(pos.y - oscillatingBlock.startPos.y) > oscillatingBlock.distanceToOscillate){
                    oscillatingBlock.changeDirection()
                }
                when(oscillatingBlock.dir){
                    direction.Up -> {
                        pos.y -= oscillatingBlock.speed
                    }
                    direction.Right -> {
                        pos.x += oscillatingBlock.speed
                    }
                    direction.Down -> {
                        pos.y += oscillatingBlock.speed
                    }
                    direction.Left -> {
                        pos.x -= oscillatingBlock.speed
                    }
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
