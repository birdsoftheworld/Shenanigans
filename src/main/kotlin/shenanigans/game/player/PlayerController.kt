package shenanigans.game.player

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3f
import org.joml.Vector3fc
import shenanigans.engine.audio.AudioClip
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.net.ClientOnly
import shenanigans.engine.physics.Collider
import shenanigans.engine.physics.CollisionEvent
import shenanigans.engine.physics.Time
import shenanigans.engine.timer.timeEventPhysics
import shenanigans.engine.util.Transform
import shenanigans.engine.util.moveTowards
import shenanigans.engine.util.raycast
import shenanigans.engine.util.shapes.Rectangle
import shenanigans.engine.window.Key
import shenanigans.engine.window.events.KeyboardState
import shenanigans.game.level.block.*
import shenanigans.game.level.component.MODIFIERS
import shenanigans.game.level.component.PlayerModifier
import shenanigans.game.level.component.SurfaceModifier
import shenanigans.game.network.Synchronized
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign
import kotlin.reflect.KClass


enum class WallStatus(val sign: Float) {
    Off(0f),
    Left(-1f),
    Right(1f)
}

data class PlayerProperties(
    var maxAcceleration: Float = 1375f,
    var maxAirAcceleration: Float = .95f * maxAcceleration,
    var maxDeceleration: Float = 1650f,
    var maxAirDeceleration: Float = 0f,
    var maxTurnSpeed: Float = 2750f,
    var maxAirTurnSpeed: Float = 2150f,

    var maxSpeed: Float = 275f,
    var crouchedMoveSpeedMultiplier: Float = 0.35f,
    var crouchedAirTurnSpeedMultiplier: Float = 0.6f,

    var jumpSpeed: Float = 550f,

    var downwardMovementMultiplier: Float = 1f,

    var wallJumpHSpeed: Float = 400f,
    var wallJumpVSpeed: Float = 500f,
    var wallSlideSpeed: Float = 140f,

    var coyoteTime: Float = .1f,
    var wallCoyoteTime: Float = .1f,
    var jumpBufferTime: Float = .2f,

    var trampolineSpeed: Float = jumpSpeed * 2f,
    var accelerationMultiplier: Float = 2f,

    var jumpCutoff: Float = 2f,

    var maxJumps: Int = 1,
    var fallingCountsAsJumping: Boolean = true,

    var terminalVelocity: Float = 825f,
    var gravity: Float = 1375f,
)

sealed class Jump(val modifiers: Set<PlayerModifier>)
class FloorJump(modifiers: Set<PlayerModifier>, isCoyote: Boolean) : Jump(modifiers)
class WallJump(modifiers: Set<PlayerModifier>, val wall: WallStatus, isCoyote: Boolean) : Jump(modifiers)
class AirJump(modifiers: Set<PlayerModifier>) : Jump(modifiers)
class FallJump(modifiers: Set<PlayerModifier>) : Jump(modifiers)
object TrampolineJump : Jump(setOf())

class Player : Component

@ClientOnly
data class ClientPlayer(
    val properties: PlayerProperties
) : Component {
    var effectiveProperties: PlayerProperties = properties.copy()

    var previousActiveSurfaces: Set<PlayerModifier> = setOf()
    var activeModifiers: MutableSet<PlayerModifier> = mutableSetOf()

    val velocity: Vector2f = Vector2f()

    var onGround: Boolean = false
    var onCeiling: Boolean = false
    val onWall: Boolean
        get() = wall != WallStatus.Off

    var wall: WallStatus = WallStatus.Off

    var crouching: Boolean = false

    var currentJump: Jump? = null
    var jumps: Int = 0

    var coyoteTime: Float = 0f
    var wallCoyoteTime: Float = 0f
    var lastWallDirectionTouched: WallStatus = WallStatus.Off

    var jumpBufferTime: Float = 0f
}

class PlayerController : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val keyboard = resources.get<KeyboardState>()
        val deltaTimeF = resources.get<Time>().deltaTime.toFloat()
        var blockMovement = Vector2f()
        query(setOf(ClientPlayer::class, Transform::class, Collider::class)).forEach { entity ->
            // shorter names
            val player = entity.component<ClientPlayer>().get()
            val transform = entity.component<Transform>()
            val velocity = player.velocity

            // reset statuses (will be set later in collisions)
            player.onGround = false
            player.wall = WallStatus.Off
            player.onCeiling = false

            // horizontal move direction
            var moveDirection = 0f
            //left
            if (keyboard.isPressed(Key.A)) {
                moveDirection -= 1f
            }
            //right
            if (keyboard.isPressed(Key.D)) {
                moveDirection += 1f
            }

            // collisions
            player.activeModifiers = mutableSetOf()
            var trampolineBounce = false
            eventQueues.own.receive(CollisionEvent::class).filter { entity.id == it.target }
                .forEach collision@{ event ->
                    val e = query(emptySet())[event.with] ?: return@collision
                    val solid = e.component<Collider>().get().solid
                    if (solid) {
                        if (event.normal.y < 0) {
                            player.onGround = true
                            velocity.y = velocity.y.coerceAtMost(0f)
                            if (e.componentOpt<TrampolineBlock>() != null) {
                                // defer until later; otherwise, player.onGround would be later set to true
                                trampolineBounce = true
                            }

                            if (e.componentOpt<SurfaceModifier>() != null) {
                                val surface = e.component<SurfaceModifier>().get()
                                if (surface.floor != null) {
                                    player.activeModifiers.add(surface.floor)
                                }
                            }
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

                        if (event.normal.x != 0f && e.componentOpt<SurfaceModifier>() != null) {
                            val surface = e.component<SurfaceModifier>().get()
                            if (surface.wall != null) {
                                player.activeModifiers.add(surface.wall)
                            }
                        }

                        if (e.componentOpt<OscillatingBlock>() != null && event.normal.y <= 0f) {
                            // if the collision isn't on a wall, or if the player
                            // is moving in the same direction as the wall surface
                            if (
                                event.normal.x == 0f
                                || (moveDirection != 0f && sign(moveDirection) != sign(event.normal.x))
                            ) {
                                val oscillatingBlock = e.component<OscillatingBlock>().get()
                                blockMovement = oscillatingBlock.getMove()
                            }
                        }
                    }
                    if (e.componentOpt<AccelerationBlock>() != null) {
                        if (!e.component<AccelerationBlock>().get().used) {
                            velocity.mul(player.effectiveProperties.accelerationMultiplier)
                            e.component<AccelerationBlock>().get().used = true
                        }
                    }

                    if (e.componentOpt<SpikeBlock>() != null) {
                        respawn(entity, query)
                    }
                }

            // modify properties based off of current surfaces and current jump
            player.effectiveProperties = player.properties.copy()
            val modifiers = mutableSetOf<PlayerModifier>()
            modifiers.addAll(player.activeModifiers)
            if (player.currentJump != null) {
                modifiers.addAll(player.currentJump!!.modifiers)
            }
            if (player.coyoteTime > 0f || player.wallCoyoteTime > 0f) {
                modifiers.addAll(player.previousActiveSurfaces.filter { it.keepDuringCoyoteTime })
            }
            modifiers.forEach {
                MODIFIERS[it.id]?.let { it1 -> it1(player.effectiveProperties) }
            }

            val properties = player.effectiveProperties

            // if a trampoline was collided with during the collision check, now bounce the player upwards
            if (trampolineBounce) {
                velocity.y = -properties.trampolineSpeed
                player.currentJump = TrampolineJump
                player.onGround = false
                player.restoreJumps()
                player.jumps--
            }

            if (player.onGround) {
                player.currentJump = null
            }

            val pos = transform.get().position

            // crouch checks
            val holdingCrouch = keyboard.isPressed(Key.S)
            if (!player.crouching && holdingCrouch) {
                player.crouching = true
                entity.changeCrouch(true)
            } else if (player.crouching && !holdingCrouch) {
                // un-crouching requires there is nothing above the player
                val things = query(setOf(Collider::class, Transform::class))
                    .filter { e -> e.id != entity.id && e.component<Collider>().get().solid }
                val nudge = 0.05f

                val topPosition = Vector2f(pos.x + nudge, pos.y)
                val height = SHAPE_BASE.height - SHAPE_CROUCHED.height
                var hit = raycast(
                    things,
                    topPosition,
                    Vector2f(0f, -1f),
                    height
                )
                if (hit == null) {
                    topPosition.add(SHAPE_CROUCHED.width - 2 * nudge, 0f)
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

            if (keyboard.isPressed(Key.R)) {
                respawn(entity, query)
            }

            val desiredVelocity = Vector2f(moveDirection * properties.maxSpeed, 0f)

            // slower movement while crouched
            if (player.crouching && player.onGround) {
                desiredVelocity.x *= properties.crouchedMoveSpeedMultiplier
            }

            // in the air: if the player is moving faster than move speed, maintain the current speed
            if (!player.onGround && velocity.x * moveDirection > desiredVelocity.x * moveDirection) {
                desiredVelocity.x = velocity.x
            }

            var turnSpeed = 0f
            var acceleration = 0f
            var deceleration = 0f

            // different variables based on whether on ground or in air
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

            // nerf air turn speed while crouching
            if (player.crouching && !player.onGround) {
                turnSpeed *= properties.crouchedAirTurnSpeedMultiplier
            }

            // maximum change for speed in this frame
            val maxSpeedChange = if (moveDirection != 0f) {
                if (desiredVelocity.x.sign != velocity.x.sign) {
                    turnSpeed
                } else if (abs(desiredVelocity.x) >= abs(velocity.x)) {
                    acceleration
                } else {
                    deceleration
                }
            } else {
                deceleration
            } * deltaTimeF

            // move
            velocity.x = moveTowards(velocity.x, desiredVelocity.x, maxSpeedChange)


            // jumps
            var jumped = false
            val pressedJump = keyboard.isJustPressed(Key.SPACE)
            val holdingJump = keyboard.isPressed(Key.SPACE)
            val bufferedJump = player.jumpBufferTime > 0f && holdingJump

            val gravity = player.getGravity(holdingJump)

            val jump = player.getJump()
            if ((pressedJump || bufferedJump) && jump != null) {
                jumped = true
                player.jump(jump)
            }
            if (pressedJump && !jumped) {
                player.jumpBufferTime = properties.jumpBufferTime
            }

            // touching a wall
            if (player.onWall) {
                if (!jumped) {
                    player.lastWallDirectionTouched = player.wall
                    player.wallCoyoteTime = properties.wallCoyoteTime
                }
                if (player.currentJump != null && player.velocity.y >= 0 && !player.crouching) {
                    player.currentJump = null
                    player.restoreJumps()
                }
            }
            if (player.onGround && !jumped) {
                player.coyoteTime = properties.coyoteTime
                player.restoreJumps()
            }

            if (!player.onGround) {
                player.coyoteTime = (player.coyoteTime - deltaTimeF).coerceAtLeast(0f)
                player.wallCoyoteTime = (player.wallCoyoteTime - deltaTimeF).coerceAtLeast(0f)
                // once coyote time ends, remove the jump
                if (properties.fallingCountsAsJumping && player.currentJump == null && player.coyoteTime <= 0 && (player.wallCoyoteTime <= 0 || player.crouching)) {
                    player.jumps--
                    player.currentJump = FallJump(player.previousActiveSurfaces)
                }
            }

            // save current surfaces, in case next frame the player is off the ground
            if (player.onGround || player.onWall) {
                player.previousActiveSurfaces = player.activeModifiers
            }

            player.jumpBufferTime = (player.jumpBufferTime - deltaTimeF).coerceAtLeast(0f)

            // apply velocity
            pos.add(velocity.x * deltaTimeF, velocity.y * deltaTimeF, 0f)

            // apply gravity
            if (!player.onGround && !jumped) {
                velocity.y += gravity * deltaTimeF

                // compensation
                pos.add(Vector3f(0f, gravity, 0f).mul(1 / 2 * deltaTimeF * deltaTimeF))
            }

            if (player.onWall && sign(velocity.x) == player.wall.sign && !holdingCrouch) {
                velocity.y = velocity.y.coerceAtMost(properties.wallSlideSpeed)
            }

            velocity.y = velocity.y.coerceAtMost(properties.terminalVelocity)

            // movement from moving blocks
            blockMovement.mul(deltaTimeF)
            pos.add(blockMovement.x, blockMovement.y, 0f)

            entity.component<ClientPlayer>().mutate()
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

    private fun ClientPlayer.getGravity(holdingJump: Boolean): Float {
        val gravityMultiplier = if (velocity.y < 0f) {
            if (!holdingJump && (this.currentJump is FloorJump || this.currentJump is WallJump)) {
                1f + effectiveProperties.jumpCutoff
            } else {
                1f
            }
        } else if (velocity.y > 0f) {
            effectiveProperties.downwardMovementMultiplier
        } else {
            1f
        }
        return effectiveProperties.gravity * gravityMultiplier
    }

    private fun ClientPlayer.restoreJumps() {
        this.jumps = effectiveProperties.maxJumps
    }

    private fun ClientPlayer.jump(jump: Jump) {
        this.currentJump = jump

        val targetSpeed = if (jump is WallJump) {
            if (jump.wall == WallStatus.Right) {
                velocity.x -= effectiveProperties.wallJumpHSpeed
            } else if (jump.wall == WallStatus.Left) {
                velocity.x += effectiveProperties.wallJumpHSpeed
            }
            effectiveProperties.wallJumpVSpeed
        } else {
            effectiveProperties.jumpSpeed
        }

        var jumpSpeed = -targetSpeed

        if (velocity.y < 0f) {
            jumpSpeed = min(jumpSpeed, velocity.y)
        }

        velocity.y = jumpSpeed

        this.jumpBufferTime = 0f
        this.coyoteTime = 0f
        this.jumps--
    }

    private fun ClientPlayer.getJump(): Jump? {
        if (this.jumps <= 0) {
            return null
        }
        return if (this.onGround) {
            FloorJump(this.activeModifiers.filter { it.keepOnJump }.toSet(), false)
        } else if (this.coyoteTime > 0f) {
            FloorJump(this.previousActiveSurfaces, true)
        } else if (this.wall != WallStatus.Off && !this.crouching) {
            WallJump(this.activeModifiers.filter { it.keepOnJump }.toSet(), this.wall, false)
        } else if (this.wallCoyoteTime > 0f && !this.crouching) {
            WallJump(this.previousActiveSurfaces, this.lastWallDirectionTouched, true)
        } else {
            return AirJump(this.previousActiveSurfaces.filter { it.keepOnJump }.toSet())
        }
    }

    companion object {
        fun createPlayer(position: Vector2fc): Sequence<Component> {
            val player = ClientPlayer(
                PlayerProperties()
            )
            val playerTransform = Transform(
                Vector3f(position.x() - SHAPE_BASE.width / 2, position.y() - SHAPE_BASE.height / 2, 100f)
            )

            return sequenceOf(
                playerTransform,
                Sprite(TEXTURE.getRegion(), SHAPE_BASE),
                Collider(SHAPE_BASE, false, tracked = true),
                player,
                Player(),
                Synchronized()
            )
        }

        fun getCameraPosition(player: EntityView): Vector3fc {
            val p = Vector3f(player.component<Transform>().get().position)
            p.x += SHAPE_BASE.width / 2
            p.y += SHAPE_BASE.height / 2
            if (player.component<ClientPlayer>().get().crouching) {
                p.y -= SHAPE_BASE.height - SHAPE_CROUCHED.height
            }
            return p
        }

        val SHAPE_BASE: Rectangle = Rectangle(40f, 72f)
        val SHAPE_CROUCHED: Rectangle = Rectangle(40f, 48f)

        val TEXTURE = TextureManager.createTexture(TextureKey("player"), "/player.png")

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