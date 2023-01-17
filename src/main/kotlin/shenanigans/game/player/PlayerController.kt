package shenanigans.game.player

import com.sun.org.apache.xpath.internal.operations.Bool
import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.util.Transform
import shenanigans.engine.window.Key
import shenanigans.engine.window.MouseButton
import shenanigans.engine.window.events.KeyboardState
import shenanigans.engine.window.events.MouseState
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round
import shenanigans.engine.events.Event
import kotlin.math.sign
import kotlin.reflect.KClass


enum class WallStatus{
    Off,
    Left,
    Right
}

data class Player(
  val groundAccel: Float = .01f,
  val airAccelRatio: Float = .02f,
  val xMax : Float = .15f,
  val jumpSpeed: Float = .2f,
  val friction : Float = 15f,
  val turnSpeed: Float = 20f,
  val drag : Float = 15f,
  var tempPos : Vector2f = Vector2f(),
  var airTurnSpeed : Float = 3f,
  var onGround : Boolean = false,
  var onRoof : Boolean = false,
  var wall : WallStatus = WallStatus.Off
) : Component{
}

class PlayerOnWallLeftEvent : Event
class PlayerOnWallRightEvent : Event
class PlayerOnGroundEvent : Event
class PlayerOnRoofEvent : Event
class PlayerController : System {
    val gravity : Float = .5f
    var velocity = Vector2f()
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Player::class, Transform::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val keyboard = resources.get<KeyboardState>()
        val deltaTime = resources.get<DeltaTime>().deltaTime
        val L = WallStatus.Left
        val R = WallStatus.Right
        val O = WallStatus.Off

        entities.forEach { entity ->
            val player = entity.component<Player>()
            resources.get<EventQueue>().iterate<PlayerOnGroundEvent>().forEach { event ->
                player.get().onGround = true
                if(velocity.y > 0f){
                    velocity.y = 0f
                }
            }
            resources.get<EventQueue>().iterate<PlayerOnWallRightEvent>().forEach { event ->
                player.get().wall = R
            }
            resources.get<EventQueue>().iterate<PlayerOnWallLeftEvent>().forEach { event ->
                player.get().wall = L
            }
            resources.get<EventQueue>().iterate<PlayerOnRoofEvent>().forEach { event ->
                player.get().onRoof = true
            }
            val transform = entity.component<Transform>()
            val pos = transform.get().position
            val xMax = player.get().xMax
            val drag = player.get().drag
            val friction = player.get().friction
            val turnSpeed = player.get().turnSpeed
            val airTurnSpeed = player.get().airTurnSpeed
            var desiredVelocity = Vector2f(0f, 0f)
            var deccel = 1f
            var turnAccel = 0f
            var maxSpeedChange = 0f
            var wantToJump = false
            val jumpSpeed = player.get().jumpSpeed
            var onGround = player.get().onGround
            var wall = player.get().wall
            var onRoof = player.get().onRoof
            var xAccel = player.get().groundAccel
            if(!onGround){
                xAccel *=player.get().airAccelRatio
            }

            //Deceleration based on whether on ground or in air
            when (onGround) {
                true -> {
                    deccel = friction;
                    turnAccel = turnSpeed
                }

                false -> {
                    deccel = drag;
                    turnAccel = airTurnSpeed
                }
            }


            //left
            if (keyboard.isPressed(Key.A)) {
                desiredVelocity.add(Vector2f(-xMax, 0f))
            }

            //right
            if (keyboard.isPressed(Key.D)) {
                desiredVelocity.add(Vector2f(xMax, 0f))
            }

            //jump
            if (keyboard.isPressed(Key.W)) {
                wantToJump = true
            }

            fun jump() {
                velocity.y = -jumpSpeed
                if(wall == R){
                    println("LEFT")
                    velocity.y = -jumpSpeed
                    velocity.x -=  jumpSpeed
                }
                if(wall == L){
                    println("RIGHT")
                    velocity.y = -jumpSpeed
                    velocity.x +=  jumpSpeed
                }
                wantToJump = false
            }

            fun canJump(): Boolean{
                return (onGround || wall != WallStatus.Off)
            }

            if (wantToJump && canJump()) {
                jump()
            }

            if(!onGround) {
                if(wall != O && velocity.y > 0){
                    velocity.y += gravity/4 * deltaTime.toFloat()
                }
                else{
                    velocity.y += gravity * deltaTime.toFloat()
                }
            }

            if (desiredVelocity.x != 0f) {
                if (desiredVelocity.x.sign != velocity.x.sign) {
                    maxSpeedChange = turnAccel * deltaTime.toFloat()
                } else {
                    maxSpeedChange = xAccel * deltaTime.toFloat()
                }
            } else {
                maxSpeedChange = deccel * deltaTime.toFloat()
            }
            velocity.x = velocity.x + (desiredVelocity.x - velocity.x) * maxSpeedChange
            if(onRoof){
                velocity.y = .00000001f
            }
            pos.add(velocity)
            transform.mutate()
            player.get().onGround = false
            player.get().wall = O
            player.get().onRoof = false

        }
    }
}