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
import kotlin.math.sign
import kotlin.reflect.KClass

data class Player(
                  val xAccel: Float = 5f,
                  val xMax : Float = .15f,
                  val jumpSpeed: Float = .2f,
                  val friction : Float = 8f,
                  val turnSpeed: Float = 20f,
                  val drag : Float = 8f,
                  var airTurnSpeed : Float = 5f,
                  var onGround : Boolean = false) : Component{
}

class PlayerController : System {
    val gravity : Float = .5f
    var velocity = Vector2f()
    var oldPos = Vector2f()

    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Player::class, Transform::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val keyboard = resources.get<KeyboardState>()
        val deltaTime = resources.get<DeltaTime>().deltaTime


        entities.forEach { entity ->
            val player = entity.component<Player>()
            resources.get<EventQueue>().iterate<PlayerOnGroundEvent>().forEach { event ->
                player.get().onGround = true
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
            val xAccel = player.get().xAccel
            var turnAccel = 0f
            var maxSpeedChange = 0f
            var wantToJump = false
            val jumpSpeed = player.get().jumpSpeed
            var onGround = player.get().onGround


            //left
            if (keyboard.isPressed(Key.A)) {
                desiredVelocity.add(Vector2f(-xMax, 0f))
            }

            //right
            if (keyboard.isPressed(Key.D)) {
                desiredVelocity.add(Vector2f(xMax, 0f))
            }
            //jump

            when (onGround) {
                true -> {
                    deccel = friction; turnAccel = turnSpeed
                }

                false -> {
                    deccel = drag; turnAccel = airTurnSpeed
                }
            }


            if (keyboard.isPressed(Key.W)) {
                wantToJump = true
            }

            fun jump() {
                velocity.y = -jumpSpeed
                wantToJump = false
                onGround = false
            }




            if (wantToJump && onGround) {
                jump()
            }
            if(!onGround) {
                velocity.y += gravity * deltaTime.toFloat()
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
            oldPos=pos
            pos.add(velocity)
            transform.mutate()
        }
    }
}