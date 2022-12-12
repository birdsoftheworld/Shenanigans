package shenanigans.game.player

import org.joml.Vector2f
import org.lwjgl.openxr.FBKeyboardTracking
import shenanigans.engine.ecs.*
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.util.Transform
import shenanigans.engine.window.Key
import shenanigans.engine.window.events.KeyboardState
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign
import kotlin.math.sqrt
import kotlin.reflect.KClass

data class Player(val xVel: Float,
                  val xAccel: Float = 5f,
                  val xMax : Float = .2f,
                  val yMax :  Float = .3f,
                  val jumpHeight: Float = 50f,
                  val jumpSpeed: Float = .2f,
                  val friction : Float = 5f,
                  val turnSpeed: Float = 20f,
                  val drag : Float = 6f,
                  var airTurnSpeed : Float = 5f) : Component

class PlayerController : System {
    val gravity : Float = .0001f;
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
            val yMax = entity.component<Player>().get().yMax
            val drag = entity.component<Player>().get().drag
            val friction = entity.component<Player>().get().friction
            var jumpSpeed = entity.component<Player>().get().jumpSpeed
            val turnSpeed = entity.component<Player>().get().turnSpeed
            val airTurnSpeed = entity.component<Player>().get().airTurnSpeed
            val jumpHeight = entity.component<Player>().get().jumpHeight
            var desiredVelocity = Vector2f(0f,0f)
            val onGround = (pos.y == 600f)
            var deccel = 1f
            val xAccel = entity.component<Player>().get().xAccel
            var turnAccel = 0f
            var maxSpeedChange = 0f
            //left
            if (keyboard.isPressed(Key.A)) {
                desiredVelocity.add(Vector2f(-xMax, 0f))
            }

            //right
            if (keyboard.isPressed(Key.D)) {
                desiredVelocity.add(Vector2f(xMax, 0f))
            }
            //jump

            when(onGround) {
                true -> {deccel = friction; turnAccel = turnSpeed;if (keyboard.isPressed(Key.W)) {
                    jumpSpeed = sqrt(-2f * gravity * jumpHeight)
                    if(velocity.y < 0f){
//                        jumpSpeed = (jumpSpeed - velocity.y, 0f)
                    }
                }}
                false -> {deccel = drag; turnAccel = airTurnSpeed}
            }

            if(desiredVelocity.x != 0f) {
                if (desiredVelocity.x.sign != velocity.x.sign) {
                    maxSpeedChange = turnAccel * deltaTime.toFloat()
                }
                else {
                    maxSpeedChange = xAccel * deltaTime.toFloat()
                }
            }
            else{
                maxSpeedChange = deccel * deltaTime.toFloat()
            }
            velocity.x = velocity.x + (desiredVelocity.x -velocity.x)*maxSpeedChange
            //println(maxSpeedChange)

//            velocity.add(Vector2f(0f,gravity))
//            //jump
//            if (keyboard.isPressed(Key.W) && still) {
//                velocity.y = -jumpSpeed
//            }
//            //left

//            //drag
//            else{
//                //friction
//                if(still && abs(velocity.x) > 0){
//                    velocity.y = abs(velocity.y)+friction*velocity.y.sign
//                }//drag
//                else if(abs(velocity.x) > 0){velocity.y = abs(velocity.y)+drag*velocity.y.sign}
//
//                //temporary not falling check
//                oldPos.y = pos.y
//            }
//
//            if (velocity.length() > 0) {
//                if(abs(velocity.x) > xMax){
//                    velocity.x = (xMax*velocity.x.sign)*deltaTime.toFloat()
//                }
//
//                if(abs(velocity.y) > yMax){
//                    println(velocity.y)
//                    velocity.y = (yMax*velocity.y.sign)*deltaTime.toFloat()
//                }
                pos.add(velocity)
                transform.mutate()

            }
        }
    }