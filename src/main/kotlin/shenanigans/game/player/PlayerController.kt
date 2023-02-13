package shenanigans.game.player

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.physics.CollisionEvent
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.util.Transform
import shenanigans.engine.util.moveTowards
import shenanigans.engine.window.Key
import shenanigans.engine.window.events.KeyboardState
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign
import kotlin.math.sqrt
import kotlin.reflect.KClass


enum class WallStatus(val sign: Float) {
    Off(0f),
    Left(-1f),
    Right(1f)
}

data class PlayerProperties(
    val maxAcceleration: Float = .50f,
    val maxAirAcceleration: Float = .75f * maxAcceleration,
    val maxDeceleration: Float = 15f,
    val maxAirDeceleration: Float = .1f,
    val maxTurnSpeed: Float = 20f,
    val maxAirTurnSpeed: Float = .5f,

    val maxSpeed: Float = .1f,

    val jumpHeight: Float = .03f,
    val timeToJumpApex: Float = .35f,

    val downwardMovementMultiplier: Float = 1.275f,

    val wallJumpDistance: Float = .125f,
    val wallJumpHeight: Float = .025f,
    val wallJumpSlideSpeed: Float = 0.05f,

    val coyoteTime: Float = .1f,
    val jumpBufferTime: Float = .2f,

    val gravity: Float = .5f,
    val terminalVelocity: Float = 50f,
)

data class Player(
    val properties: PlayerProperties,
    val velocity: Vector2f = Vector2f(),
    var onGround: Boolean = false,
    var wall: WallStatus = WallStatus.Off,
    var onCeiling: Boolean = false,
    var coyoteTime: Float = 0f,
    var jumpBufferTime: Float = 0f
) : Component

class PlayerController : System {

    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Player::class, Transform::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val keyboard = resources.get<KeyboardState>()
        val deltaTime = resources.get<DeltaTime>().deltaTime

        entities.forEach { entity ->
            val player = entity.component<Player>().get()
            val transform = entity.component<Transform>()

            val velocity = player.velocity

            player.onGround = false
            player.wall = WallStatus.Off
            player.onCeiling = false

            resources.get<EventQueue>().iterate<CollisionEvent>().forEach { event ->
                if (entity.id == event.target) {
                    if (event.normal.y < 0) {
                        player.onGround = true
                        velocity.y = velocity.y.coerceAtMost(0f)
                    } else if (event.normal.y > 0) {
                        player.onCeiling = true
                        velocity.y = max(0f, velocity.y)
                    }
                    if (event.normal.x < 0) {
                        player.wall = WallStatus.Right
                        velocity.x = velocity.x.coerceAtMost(0f)
                    } else if (event.normal.x > 0) {
                        player.wall = WallStatus.Left
                        velocity.x = velocity.x.coerceAtLeast(0f)
                    }
                }
            }

            val pos = transform.get().position

            val properties = player.properties

            val desiredVelocity = Vector2f(0f, 0f)

            var direction = 0f
            //left
            if (keyboard.isPressed(Key.A)) {
                direction -= 1f
            }
            //right
            if (keyboard.isPressed(Key.D)) {
                direction += 1f
            }
            desiredVelocity.add(direction * properties.maxSpeed, 0f)

            var turnSpeed = 0f
            var acceleration = 0f
            var deceleration = 0f

            //Deceleration based on whether on ground or in air
            when (player.onGround) {
                true -> {
                    acceleration = properties.maxAcceleration
                    deceleration = properties.maxDeceleration
                    turnSpeed = properties.maxTurnSpeed
                }

                false -> {
                    acceleration = properties.maxAirAcceleration
                    deceleration = properties.maxAirDeceleration
                    turnSpeed = properties.maxAirTurnSpeed
                }
            }

            val maxSpeedChange = if (direction != 0f) {
                if (desiredVelocity.x.sign != velocity.x.sign) {
                    turnSpeed
                } else {
                    acceleration
                }
            } else {
                deceleration
            } * deltaTime.toFloat()

            velocity.x = moveTowards(velocity.x, desiredVelocity.x, maxSpeedChange)

            val gravityMultiplier = if (velocity.y > 0f) {
                properties.downwardMovementMultiplier
            } else {
                1f
            }
            val adjustedGravity = (-2f * properties.jumpHeight) / (properties.timeToJumpApex * properties.timeToJumpApex)
            val gravityScale = (adjustedGravity / -properties.gravity) * gravityMultiplier

            var jumped = false
            val pressedJump = keyboard.isJustPressed(Key.W)
            val bufferedJump = player.jumpBufferTime > 0f && keyboard.isPressed(Key.W)

            if ((pressedJump || bufferedJump) && player.canJump()) {
                jumped = true
                player.jump(gravityScale)
            }
            if (pressedJump && !jumped) {
                player.jumpBufferTime = properties.jumpBufferTime
            }

            if (player.onGround && !jumped) {
                player.coyoteTime = properties.coyoteTime
            } else {
                player.coyoteTime = (player.coyoteTime - deltaTime.toFloat()).coerceAtLeast(0f)
            }
            player.jumpBufferTime = (player.jumpBufferTime - deltaTime.toFloat()).coerceAtLeast(0f)

            if (!player.onGround) {
                velocity.y += properties.gravity * gravityScale * deltaTime.toFloat()
            }

            if (player.wall != WallStatus.Off && sign(velocity.x) == player.wall.sign) {
                velocity.y = velocity.y.coerceAtMost(properties.wallJumpSlideSpeed)
            }

            pos.add(velocity)
            transform.mutate()
        }
    }

    private fun Player.jump(gravityScale: Float) {
        val targetHeight = if (!this.onGround) {
            if (this.wall == WallStatus.Right) {
                velocity.x -= properties.wallJumpDistance
            } else if (this.wall == WallStatus.Left) {
                velocity.x += properties.wallJumpDistance
            }
            properties.wallJumpHeight
        } else {
            properties.jumpHeight
        }

        var jumpSpeed = sqrt(2f * properties.gravity * gravityScale * targetHeight)
        if (velocity.y < 0f) {
            jumpSpeed = max(jumpSpeed + velocity.y, 0f)
        } else if (velocity.y > 0f) {
            jumpSpeed += abs(velocity.y)
        }

        velocity.y -= jumpSpeed

        this.jumpBufferTime = 0f
        this.coyoteTime = 0f
    }

    private fun Player.canJump(): Boolean {
        return (this.onGround || this.wall != WallStatus.Off || this.coyoteTime > 0f)
    }
}