package shenanigans.game.player

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.physics.CollisionEvent
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.util.Transform
import shenanigans.engine.window.Key
import shenanigans.engine.window.events.KeyboardState
import kotlin.math.max
import kotlin.math.sign
import kotlin.reflect.KClass


enum class WallStatus {
    Off,
    Left,
    Right
}

data class PlayerProperties(
    val groundAccel: Float = 500f,
    val airAccelRatio: Float = .04f,
    val xMax: Float = .07f,
    val jumpSpeed: Float = .15f,
    val friction: Float = 15f,
    val drag: Float = 3f,
    val turnSpeed: Float = 20f,
    val airTurnSpeed: Float = 5f,
    val wallJumpDistance: Float = jumpSpeed,
    val coyoteTime: Float = .1f,
    val jumpBufferTime: Float = .25f,
    val gravity: Float = .5f,
    val terminalVelocity: Float = 50f,
    val wallJumpSlideSpeed: Float = 4f
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
                        if (velocity.y > 0f) {
                            velocity.y = 0f
                        }
                    } else if (event.normal.y > 0) {
                        player.onCeiling = true
                    }
                    if (event.normal.x < 0) {
                        player.wall = WallStatus.Right
                    } else if (event.normal.x > 0) {
                        player.wall = WallStatus.Left
                    }
                }
            }

            val pos = transform.get().position

            val properties = player.properties
            val desiredVelocity = Vector2f(0f, 0f)

            var backwardsAcceleration = 0f
            var turnAcceleration = 0f
            var acceleration = properties.groundAccel

            //Deceleration based on whether on ground or in air
            when (player.onGround) {
                true -> {
                    backwardsAcceleration = properties.friction
                    turnAcceleration = properties.turnSpeed
                }

                false -> {
                    acceleration *= properties.airAccelRatio
                    backwardsAcceleration = properties.drag
                    turnAcceleration = properties.airTurnSpeed
                }
            }

            //left
            if (keyboard.isPressed(Key.A)) {
                desiredVelocity.add(Vector2f(-properties.xMax, 0f))
            }

            //right
            if (keyboard.isPressed(Key.D)) {
                desiredVelocity.add(Vector2f(properties.xMax, 0f))
            }

            var jumped = false
            if ((keyboard.isJustPressed(Key.W) || player.jumpBufferTime > 0f) && player.canJump()) {
                jumped = true
                player.jump()
            }
            if (!jumped && keyboard.isJustPressed(Key.W) && !player.canJump()) {
                player.jumpBufferTime = properties.jumpBufferTime
            }

            val speed = if (desiredVelocity.x != 0f) {
                if (desiredVelocity.x.sign != velocity.x.sign) {
                    turnAcceleration
                } else {
                    acceleration
                }
            } else {
                backwardsAcceleration
            } * deltaTime.toFloat()

            velocity.x = velocity.x + (desiredVelocity.x - velocity.x) * speed

            if (player.onGround && !jumped) {
                player.coyoteTime = properties.coyoteTime
            } else {
                player.coyoteTime = (player.coyoteTime - deltaTime.toFloat()).coerceAtLeast(0f)
            }
            player.jumpBufferTime = (player.jumpBufferTime - deltaTime.toFloat()).coerceAtLeast(0f)

            if (player.onCeiling) {
                velocity.y = max(0f, velocity.y)
            }

            if (!player.onGround && !jumped) {
                velocity.y += if (player.wall != WallStatus.Off) {
                    properties.gravity / properties.wallJumpSlideSpeed
                } else {
                    properties.gravity
                } * deltaTime.toFloat()
            }

            pos.add(velocity)
            transform.mutate()
        }
    }

    private fun Player.jump() {
        velocity.y = -properties.jumpSpeed
        if (!this.onGround) {
            if (this.wall == WallStatus.Right) {
                velocity.x -= properties.wallJumpDistance
            } else if (this.wall == WallStatus.Left) {
                velocity.x += properties.wallJumpDistance
            }
        }

        this.jumpBufferTime = 0f
        this.coyoteTime = 0f
    }

    private fun Player.canJump(): Boolean {
        return (this.onGround || this.wall != WallStatus.Off || this.coyoteTime > 0f)
    }
}