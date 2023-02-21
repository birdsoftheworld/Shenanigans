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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.reflect.KClass


enum class WallStatus(val sign: Float) {
    Off(0f),
    Left(-1f),
    Right(1f)
}

data class PlayerProperties(
    val maxAcceleration: Float = .5f,
    val maxAirAcceleration: Float = .75f * maxAcceleration,
    val maxDeceleration: Float = .6f,
    val maxAirDeceleration: Float = .1f,
    val maxTurnSpeed: Float = 1f,
    val maxAirTurnSpeed: Float = .75f,

    val maxSpeed: Float = .1f,

    val jumpSpeed: Float = .2f,

    val downwardMovementMultiplier: Float = 1f,

    val wallJumpDistance: Float = .1f,
    val wallJumpSpeed: Float = .175f,
    val wallJumpSlideSpeed: Float = .05f,

    val coyoteTime: Float = .1f,
    val wallCoyoteTime: Float = .085f,
    val jumpBufferTime: Float = .2f,

    val jumpCutoff: Float = .75f,

    val terminalVelocity: Float = .3f,
    val gravity: Float = .5f,
)

enum class JumpType {
    Wall,
    Floor,
    None
}

data class Player(
    val properties: PlayerProperties,
    val velocity: Vector2f = Vector2f(),
    var onGround: Boolean = false,
    var wall: WallStatus = WallStatus.Off,
    var currentJump: JumpType = JumpType.None,
    var onCeiling: Boolean = false,
    var coyoteTime: Float = 0f,
    var wallCoyoteTime: Float = 0f,
    var jumpBufferTime: Float = 0f
) : Component

class PlayerController : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Player::class, Transform::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val keyboard = resources.get<KeyboardState>()
        val deltaTimeF = resources.get<DeltaTime>().deltaTime.toFloat()

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

            if (player.onGround) {
                player.currentJump = JumpType.None
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
            } * deltaTimeF

            velocity.x = moveTowards(velocity.x, desiredVelocity.x, maxSpeedChange)

            var jumped = false
            val pressedJump = keyboard.isJustPressed(Key.W)
            val holdingJump = keyboard.isPressed(Key.W)
            val bufferedJump = player.jumpBufferTime > 0f && holdingJump

            val gravityScale = player.getGravity(holdingJump)

            val jumpType = player.getJumpType()
            if ((pressedJump || bufferedJump) && jumpType != JumpType.None) {
                jumped = true
                player.jump(jumpType)
            }
            if (pressedJump && !jumped) {
                player.jumpBufferTime = properties.jumpBufferTime
            }

            if (player.wall != WallStatus.Off && !jumped) {
                player.wallCoyoteTime = properties.wallCoyoteTime
            }
            if (player.onGround && !jumped) {
                player.coyoteTime = properties.coyoteTime
            }

            if (!player.onGround) {
                player.coyoteTime = (player.coyoteTime - deltaTimeF).coerceAtLeast(0f)
                player.wallCoyoteTime = (player.wallCoyoteTime - deltaTimeF).coerceAtLeast(0f)
            }
            player.jumpBufferTime = (player.jumpBufferTime - deltaTimeF).coerceAtLeast(0f)

            if (!player.onGround && !jumped) {
                velocity.y += gravityScale * deltaTimeF
            }

            if (player.wall != WallStatus.Off && sign(velocity.x) == player.wall.sign) {
                velocity.y = velocity.y.coerceAtMost(properties.wallJumpSlideSpeed)
            }

            velocity.y = velocity.y.coerceAtMost(properties.terminalVelocity)

            pos.add(velocity)
            transform.mutate()
        }
    }

    private fun Player.getGravity(holdingJump: Boolean): Float {
        val gravityMultiplier = if (velocity.y < 0f) {
            if (holdingJump && this.currentJump != JumpType.None) {
                1f
            } else {
                1f + properties.jumpCutoff
            }
        } else if (velocity.y > 0f) {
            properties.downwardMovementMultiplier
        } else {
            1f
        }
        return properties.gravity * gravityMultiplier
    }

    private fun Player.jump(jumpType: JumpType) {
        this.currentJump = jumpType

        val targetSpeed = if (jumpType == JumpType.Wall) {
            if (this.wall == WallStatus.Right) {
                velocity.x -= properties.wallJumpDistance
            } else if (this.wall == WallStatus.Left) {
                velocity.x += properties.wallJumpDistance
            }
            properties.wallJumpSpeed
        } else {
            properties.jumpSpeed
        }

        var jumpSpeed = -targetSpeed
        if (velocity.y < 0f) {
            jumpSpeed = min(jumpSpeed, velocity.y)
        }

        velocity.y = jumpSpeed

        this.jumpBufferTime = 0f
        this.coyoteTime = 0f
    }

    private fun Player.getJumpType(): JumpType {
        if (this.onGround || this.coyoteTime > 0f) {
            return JumpType.Floor
        } else if (this.wall != WallStatus.Off || this.wallCoyoteTime > 0f) {
            return JumpType.Wall
        }
        return JumpType.None
    }
}