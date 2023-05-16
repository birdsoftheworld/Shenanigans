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

@ClientOnly
data class Player(
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
        query(setOf(Player::class, Transform::class, Collider::class)).forEach { entity ->
            val player = entity.component<Player>().get()
            val transform = entity.component<Transform>()

            val velocity = player.velocity

            player.onGround = false
            player.wall = WallStatus.Off
            player.onCeiling = false

            val properties = player.effectiveProperties

            var moveDirection = 0f
            //left
            if (keyboard.isPressed(Key.A)) {
                moveDirection -= 1f
            }
            //right
            if (keyboard.isPressed(Key.D)) {
                moveDirection += 1f
            }

            player.activeModifiers = mutableSetOf()
            var trampolineBounce = false
            eventQueues.own.receive(CollisionEvent::class).filter { entity.id == it.target }.forEach collision@{ event ->
                val e = query(emptySet())[event.with] ?: return@collision
                val solid = e.component<Collider>().get().solid
                if (solid) {
                    if (event.normal.y < 0) {
                        player.onGround = true
                        velocity.y = velocity.y.coerceAtMost(0f)
                        if (e.componentOpt<TrampolineBlock>() != null) {
                            trampolineBounce = true
                        }

                        if (e.componentOpt<SurfaceModifier>() != null) {
                            val surface = e.component<SurfaceModifier>().get()
                            if(surface.floor != null) {
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
                        if(surface.wall != null) {
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
                if(e.componentOpt<AccelerationBlock>() != null){
                    lifecycle.del(e.id)
                    velocity.mul(properties.accelerationMultiplier)
                    velocity.x = velocity.x.coerceAtMost(properties.terminalVelocity)
                    velocity.y = velocity.y.coerceAtMost(properties.terminalVelocity)
                }
                if (e.componentOpt<SpikeBlock>() != null) {
                    respawn(entity, query)
                }
                if (e.componentOpt<GoalBlock>() != null) {
                    println("CONGRATS YOU PASSED THE LEVEL")
                    respawn(entity, query)
                }
            }

            player.effectiveProperties = player.properties.copy()
            val modifiers = mutableSetOf<PlayerModifier>()
            modifiers.addAll(player.activeModifiers)
            if(player.currentJump != null) {
                modifiers.addAll(player.currentJump!!.modifiers)
            }
            if(player.coyoteTime > 0f || player.wallCoyoteTime > 0f) {
                modifiers.addAll(player.previousActiveSurfaces.filter { it.keepDuringCoyoteTime })
            }
            modifiers.forEach {
                MODIFIERS[it.id]?.let { it1 -> it1(player.effectiveProperties) }
            }

            if(trampolineBounce) {
                velocity.y = -properties.trampolineSpeed
                player.currentJump = TrampolineJump
                player.onGround = false
                player.restoreJumps()
            }

            if (player.onGround) {
                player.currentJump = null
            }

            val pos = transform.get().position

            val holdingCrouch = keyboard.isPressed(Key.S)
            if (!player.crouching && holdingCrouch) {
                player.crouching = true
                entity.changeCrouch(true)
            } else if (player.crouching && !holdingCrouch) {
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
            if (!player.onGround && velocity.x * moveDirection > desiredVelocity.x * moveDirection) {
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

            if(player.crouching) {
                turnSpeed *= properties.crouchedAirTurnSpeedMultiplier
            }

            val maxSpeedChange = if (moveDirection != 0f) {
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
                player.jump(jump)
            }
            if (pressedJump && !jumped) {
                player.jumpBufferTime = properties.jumpBufferTime
            }

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
                if (properties.fallingCountsAsJumping && player.currentJump == null && player.coyoteTime <= 0 && (player.wallCoyoteTime <= 0 || player.crouching)) {
                    player.jumps--
                    player.currentJump = FallJump(player.previousActiveSurfaces)
                }
            }

            if (player.onGround || player.onWall) {
                player.previousActiveSurfaces = player.activeModifiers
            }

            player.jumpBufferTime = (player.jumpBufferTime - deltaTimeF).coerceAtLeast(0f)

            pos.add(velocity.x * deltaTimeF, velocity.y * deltaTimeF, 0f)

            if (!player.onGround && !jumped) {
                velocity.y += gravity * deltaTimeF
                pos.add(Vector3f(0f, gravity, 0f).mul(1 / 2 * deltaTimeF * deltaTimeF))
            }

            if (player.onWall && sign(velocity.x) == player.wall.sign && !holdingCrouch) {
                velocity.y = velocity.y.coerceAtMost(properties.wallSlideSpeed)
            }

            velocity.y = velocity.y.coerceAtMost(properties.terminalVelocity)

            blockMovement.mul(deltaTimeF)
            pos.add(blockMovement.x, blockMovement.y, 0f)

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

    private fun Player.restoreJumps() {
        this.jumps = effectiveProperties.maxJumps
    }

    private fun Player.jump(jump: Jump) {
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

    private fun Player.getJump(): Jump? {
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
            val player = Player(
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
                Synchronized()
            )
        }

        fun getCameraPosition(player: EntityView): Vector3fc {
            val p = Vector3f(player.component<Transform>().get().position)
            p.x += SHAPE_BASE.width / 2
            p.y += SHAPE_BASE.height / 2
            if (player.component<Player>().get().crouching) {
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