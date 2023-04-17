package shenanigans.game.player

import org.joml.Vector2f
import org.joml.Vector3f
import shenanigans.engine.audio.AudioClip
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.net.ClientOnly
import shenanigans.engine.physics.Collider
import shenanigans.engine.physics.CollisionEvent
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.util.Transform
import shenanigans.engine.util.moveTowards
import shenanigans.engine.util.raycast
import shenanigans.engine.util.shapes.Rectangle
import shenanigans.engine.window.Key
import shenanigans.engine.window.events.KeyboardState
import shenanigans.game.level.block.*
import kotlin.math.min
import kotlin.math.sign
import kotlin.reflect.KClass


enum class WallStatus(val sign: Float) {
    Off(0f),
    Left(-1f),
    Right(1f)
}

data class PlayerProperties(
    val maxAcceleration: Float = 1375f,
    val maxAirAcceleration: Float = .95f * maxAcceleration,
    val maxDeceleration: Float = 1650f,
    val maxAirDeceleration: Float = 0f,
    val maxTurnSpeed: Float = 2750f,
    val maxAirTurnSpeed: Float = 2150f,

    val maxSpeed: Float = 275f,
    val crouchedMoveSpeedMultiplier: Float = 0.35f,

    val jumpSpeed: Float = 550f,

    val downwardMovementMultiplier: Float = 1f,

    val wallJumpHSpeed: Float = 400f,
    val wallJumpVSpeed: Float = 500f,
    val wallJumpSlideSpeed: Float = 140f,

    val coyoteTime: Float = .1f,
    val wallCoyoteTime: Float = .1f,
    val jumpBufferTime: Float = .2f,

    val slipperyMovementMultiplier: Float = .4f,
    val slipperyTurnSpeedMultiplier: Float = .2f,
    val slipperyDecelerationMultiplier: Float = .01f,
    val trampolineSpeed: Float = jumpSpeed * 2f,

    val jumpCutoff: Float = 2f,

    val maxJumps: Int = 1,
    val fallingCountsAsJumping: Boolean = true,

    val terminalVelocity: Float = 825f,
    val gravity: Float = 1375f,
)

sealed class Jump(val isCoyote: Boolean)
class FloorJump(isCoyote: Boolean) : Jump(isCoyote)
class WallJump(val wall: WallStatus, isCoyote: Boolean) : Jump(isCoyote)
object AirJump : Jump(false)
object FallJump : Jump(false)
object TrampolineJump : Jump(false)

@ClientOnly
data class Player(
    val properties: PlayerProperties,

    val velocity: Vector2f = Vector2f(),

    var onGround: Boolean = false,
    var onCeiling: Boolean = false,
    var wall: WallStatus = WallStatus.Off,

    var crouching: Boolean = false,

    var currentJump: Jump? = null,
    var jumps: Int = 0,

    var coyoteTime: Float = 0f,
    var wallCoyoteTime: Float = 0f,
    var lastWallDirectionTouched: WallStatus = WallStatus.Off,

    var jumpBufferTime: Float = 0f
) : Component

class PlayerController : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val keyboard = resources.get<KeyboardState>()
        val deltaTimeF = resources.get<DeltaTime>().deltaTime.toFloat()

        query(setOf(Player::class, Transform::class, Collider::class)).forEach { entity ->
            val player = entity.component<Player>().get()
            val transform = entity.component<Transform>()

            val velocity = player.velocity
            var sticky = false
            var slippery = false

            player.onGround = false
            player.wall = WallStatus.Off
            player.onCeiling = false

            val properties = player.properties

            eventQueues.own.receive(CollisionEvent::class).forEach { event ->
                val e = query(emptySet())[event.with]!!
                if (entity.id == event.target) {
                    val solid = e.component<Collider>().get().solid
                    if (solid) {
                        if (event.normal.y < 0) {
                            player.onGround = true
                            velocity.y = velocity.y.coerceAtMost(0f)
                        } else if (event.normal.y > 0) {
                            player.onCeiling = true
                            velocity.y = velocity.y.coerceAtLeast(0f)
                        }
                        if (event.normal.x < 0) {
                            player.wall = WallStatus.Right
                            velocity.x = velocity.x.coerceAtMost(0f)
                        } else if (event.normal.x > 0) {
                            player.wall = WallStatus.Left
                            velocity.x = velocity.x.coerceAtLeast(0f)
                        }
                    }
                    if (e.componentOpt<SpikeBlock>() != null) {
                        respawn(entity, query)
                    }
                    if (e.componentOpt<StickyBlock>() != null) {
                        sticky = true
                    }
                    if (e.componentOpt<SlipperyBlock>() != null) {
                        slippery = true
                    }
                    if (e.componentOpt<TrampolineBlock>() != null) {
                        velocity.y = -properties.trampolineSpeed
                        player.currentJump = TrampolineJump
                    }
                    val teleporter = e.componentOpt<TeleporterBlock>()
                    if (teleporter != null && teleporter.get().num % 2 == 0) {
                        // fixme
                    }
                }
            }

            if (player.onGround) {
                player.currentJump = null
            }

            val pos = transform.get().position

            val holdingCrouch = keyboard.isPressed(Key.S)
            if (!player.crouching && holdingCrouch && player.onGround) {
                player.crouching = true
                entity.changeCrouch(true)
            } else if (player.crouching && !holdingCrouch) {
                val things = query(setOf(Collider::class, Transform::class))
                    .filter { e -> e.id != entity.id && e.component<Collider>().get().solid }
                val topPosition = Vector2f(pos.x, pos.y)
                val height = SHAPE_BASE.height - SHAPE_CROUCHED.height
                var hit = raycast(
                    things,
                    topPosition,
                    Vector2f(0f, -1f),
                    height
                )
                if (hit == null) {
                    topPosition.add(SHAPE_CROUCHED.width, 0f)
                    hit = raycast(
                        things,
                        topPosition,
                        Vector2f(0f, -1f),
                        height
                    )
                }
                if (hit == null) {
                    player.crouching = false
                    entity.changeCrouch(false)
                }
            }

            var direction = 0f
            //left
            if (keyboard.isPressed(Key.A)) {
                direction -= 1f
            }
            //right
            if (keyboard.isPressed(Key.D)) {
                direction += 1f
            }

            if (keyboard.isPressed(Key.R)) {
                respawn(entity, query)
            }

            val desiredVelocity = Vector2f(direction * properties.maxSpeed, 0f)
            if (!player.onGround && velocity.x * direction > desiredVelocity.x * direction) {
                desiredVelocity.x = velocity.x
            }

            if (player.crouching && player.onGround) {
                desiredVelocity.x *= properties.crouchedMoveSpeedMultiplier
            }

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

            if (slippery) {
                acceleration *= properties.slipperyMovementMultiplier
                deceleration *= properties.slipperyDecelerationMultiplier
                turnSpeed *= properties.slipperyTurnSpeedMultiplier
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
            val pressedJump = keyboard.isJustPressed(Key.SPACE)
            val holdingJump = keyboard.isPressed(Key.SPACE)
            val bufferedJump = player.jumpBufferTime > 0f && holdingJump

            val gravity = player.getGravity(holdingJump)

            val jump = player.getJump()
            if ((pressedJump || bufferedJump) && jump != null) {
                jumped = true
                player.jump(jump, sticky)
            }
            if (pressedJump && !jumped) {
                player.jumpBufferTime = properties.jumpBufferTime
            }

            if (player.wall != WallStatus.Off) {
                if (!jumped) {
                    player.lastWallDirectionTouched = player.wall
                    player.wallCoyoteTime = properties.wallCoyoteTime
                }
                if (player.currentJump != null && player.velocity.y >= 0 && !player.crouching) {
                    player.currentJump = null
                    player.jumps = properties.maxJumps
                }
            }
            if (player.onGround && !jumped) {
                player.coyoteTime = properties.coyoteTime
                player.jumps = properties.maxJumps
            }

            if (!player.onGround) {
                player.coyoteTime = (player.coyoteTime - deltaTimeF).coerceAtLeast(0f)
                player.wallCoyoteTime = (player.wallCoyoteTime - deltaTimeF).coerceAtLeast(0f)
                if (properties.fallingCountsAsJumping && player.currentJump == null && player.coyoteTime <= 0 && player.wallCoyoteTime <= 0) {
                    player.jumps--
                    player.currentJump = FallJump
                }
            }
            player.jumpBufferTime = (player.jumpBufferTime - deltaTimeF).coerceAtLeast(0f)

            pos.add(velocity.x * deltaTimeF, velocity.y * deltaTimeF, 0f)

            if (!player.onGround && !jumped) {
                velocity.y += gravity * deltaTimeF
                pos.add(Vector3f(0f, gravity, 0f).mul(1/2 * deltaTimeF * deltaTimeF))
            }

            if (player.wall != WallStatus.Off && sign(velocity.x) == player.wall.sign && !holdingCrouch) {
                velocity.y = velocity.y.coerceAtMost(properties.wallJumpSlideSpeed)
            }

            velocity.y = velocity.y.coerceAtMost(properties.terminalVelocity)
            entity.component<Player>().mutate()
            transform.mutate()
        }
    }

    private fun EntityView.changeCrouch(crouched: Boolean) {
        val shape = if (crouched) SHAPE_CROUCHED else SHAPE_BASE
        val otherShape = if (crouched) SHAPE_BASE else SHAPE_CROUCHED

        val storedTransform = this.component<Transform>()
        storedTransform.get().position.y -= shape.height - otherShape.height
        storedTransform.mutate()

        val storedCollider = this.component<Collider>()
        storedCollider.get().polygon = shape
        storedCollider.mutate()

        val storedSprite = this.component<Sprite>()
        storedSprite.get().rectangle = shape
        storedSprite.mutate()
    }

    private fun Player.getGravity(holdingJump: Boolean): Float {
        val gravityMultiplier = if (velocity.y < 0f) {
            if (!holdingJump && (this.currentJump is FloorJump || this.currentJump is WallJump)) {
                1f + properties.jumpCutoff
            } else {
                1f
            }
        } else if (velocity.y > 0f) {
            properties.downwardMovementMultiplier
        } else {
            1f
        }
        return properties.gravity * gravityMultiplier
    }

    private fun Player.jump(jump: Jump, sticky : Boolean) {
        this.currentJump = jump

        val targetSpeed = if (jump is WallJump) {
            if (jump.wall == WallStatus.Right) {
                velocity.x -= properties.wallJumpHSpeed
            } else if (jump.wall == WallStatus.Left) {
                velocity.x += properties.wallJumpHSpeed
            }
            properties.wallJumpVSpeed
        } else {
            properties.jumpSpeed
        }

        var jumpSpeed = -targetSpeed
        if(sticky) {
            jumpSpeed *= .5f
        }
        if (velocity.y < 0f) {
            jumpSpeed = min(jumpSpeed, velocity.y)
        }

        velocity.y = jumpSpeed

        this.jumpBufferTime = 0f
        this.coyoteTime = 0f
        this.jumps--
    }

    private fun Player.getJump(): Jump? {
        if (this.jumps <= 0) {
            return null
        }
        return if (this.onGround) {
            FloorJump(false)
        } else if (this.coyoteTime > 0f) {
            FloorJump(true)
        } else if (this.wall != WallStatus.Off && !this.crouching) {
            WallJump(this.wall, false)
        } else if (this.wallCoyoteTime > 0f && !this.crouching) {
            WallJump(this.lastWallDirectionTouched, true)
        } else {
            return AirJump
        }
    }

    companion object {
        val SHAPE_BASE: Rectangle = Rectangle(40f, 70f)
        val SHAPE_CROUCHED: Rectangle = Rectangle(40f, 40f)

        val AUDIO_JUMP = AudioClip.fromFile("/jump.wav")
    }

    private fun respawn(player: EntityView, query: (Iterable<KClass<out Component>>) -> QueryView) {
        query(setOf(RespawnBlock::class, Transform::class)).forEach { entity ->
            val component = player.component<Transform>()
            val playerTransform = component.get()
            val blockTransform = entity.component<Transform>().get()

            val targetPos = blockTransform.position
            val currentPos = playerTransform.position
            player.component<Transform>().get().position.add(
                targetPos.x - currentPos.x,
                targetPos.y - currentPos.y,
                0f
            )
            component.mutate()
        }
    }

    private fun teleport(entityA: EntityView, target: Vector3f) {
        val currentPos = entityA.component<Transform>().get().position
        entityA.component<Transform>().get().position.add(target.x - currentPos.x, target.y - currentPos.y, 0f)
    }
}