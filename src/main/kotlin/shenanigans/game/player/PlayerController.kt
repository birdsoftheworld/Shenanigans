package shenanigans.game.player

import org.joml.Vector2f
import org.lwjgl.openxr.FBKeyboardTracking
import shenanigans.engine.ecs.*
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.util.Transform
import shenanigans.engine.window.Key
import shenanigans.engine.window.events.KeyboardState
import kotlin.jvm.internal.Ref.FloatRef
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign
import kotlin.math.sqrt
import kotlin.reflect.KClass

data class Player(val xVel: Float,
                  val xAccel: Float = 5f,
                  val xMax : Float = .15f,
                  val jumpHeight: Float = 1f,
                  val jumpSpeed: Float = .2f,
                  val timeToJump: Float = 1f,
                  val friction : Float = 8f,
                  val turnSpeed: Float = 20f,
                  val drag : Float = 8f,
                  var airTurnSpeed : Float = 5f) : Component

class PlayerController : System {
    val gravity : Float = .5f;
    var velocity = Vector2f()

    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Player::class, Transform::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val keyboard = resources.get<KeyboardState>()
        val deltaTime = resources.get<DeltaTime>().deltaTime

        entities.forEach { entity ->

            val transform = entity.component<Transform>()
            val pos = transform.get().position
            val xMax = entity.component<Player>().get().xMax
            val drag = entity.component<Player>().get().drag
            val friction = entity.component<Player>().get().friction
            val turnSpeed = entity.component<Player>().get().turnSpeed
            val airTurnSpeed = entity.component<Player>().get().airTurnSpeed
            val jumpHeight: Float = entity.component<Player>().get().jumpHeight
            val timeToJump = entity.component<Player>().get().timeToJump
            var desiredVelocity = Vector2f(0f, 0f)
            val onGround = true
            var deccel = 1f
            val xAccel = entity.component<Player>().get().xAccel
            var turnAccel = 0f
            var maxSpeedChange = 0f
            var wantToJump = false
            val jumpSpeed = entity.component<Player>().get().jumpSpeed


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
            }

            if (wantToJump && abs(pos.y-570) < .01) {
                jump()
            }
            if(pos.y < 570){
                if(velocity.y > 0){
                    velocity.y += gravity*deltaTime.toFloat()*.5f
                }
                velocity.y += gravity*deltaTime.toFloat()
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

            pos.add(velocity)
            println(pos.y)
            println(velocity.y)
            transform.mutate()

        }
    }
}