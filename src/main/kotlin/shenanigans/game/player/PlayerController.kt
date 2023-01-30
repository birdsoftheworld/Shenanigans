package shenanigans.game.player

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.Event
import shenanigans.engine.events.EventQueue
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.term.Logger
import shenanigans.engine.util.Transform
import shenanigans.engine.window.Key
import shenanigans.engine.window.events.KeyboardState
import kotlin.math.sign
import kotlin.reflect.KClass


enum class WallStatus{
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
)

data class Player(
    val properties: PlayerProperties,
    val velocity: Vector2f = Vector2f(),
    var onGround : Boolean = false,
    var onRoof : Boolean = false,
    var wall : WallStatus = WallStatus.Off
) : Component

class PlayerOnWallLeftEvent : Event
class PlayerOnWallRightEvent : Event
class PlayerOnGroundEvent : Event
class PlayerOnRoofEvent : Event

class PlayerController : System {
    val gravity : Float = .5f

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

            resources.get<EventQueue>().iterate<PlayerOnGroundEvent>().forEach { event ->
                player.onGround = true
                if(velocity.y > 0f){
                    velocity.y = 0f
                }
            }
            resources.get<EventQueue>().iterate<PlayerOnWallRightEvent>().forEach { event ->
                player.wall = WallStatus.Right
            }
            resources.get<EventQueue>().iterate<PlayerOnWallLeftEvent>().forEach { event ->
                player.wall = WallStatus.Left
            }
            resources.get<EventQueue>().iterate<PlayerOnRoofEvent>().forEach { event ->
                player.onRoof = true
            }

            Logger.log("player", "on ground: ${player.onGround}")

            val pos = transform.get().position

            val properties = player.properties
            val desiredVelocity = Vector2f(0f, 0f)

            var backwardsAcceleration = 0f
            var turnAcceleration = 0f
            var acceleration = properties.groundAccel

            if(!player.onGround) {
                acceleration *= player.properties.airAccelRatio
            }

            //Deceleration based on whether on ground or in air
            when (player.onGround) {
                true -> {
                    backwardsAcceleration = properties.friction
                    turnAcceleration = properties.turnSpeed
                }

                false -> {
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

            if (keyboard.isPressed(Key.W) && player.canJump()) {
                player.jump()
            }

            if(!player.onGround) {
                if(player.wall != WallStatus.Off && velocity.y > 0){
                    velocity.y += gravity/4 * deltaTime.toFloat()
                } else {
                    velocity.y += gravity * deltaTime.toFloat()
                }
            }

            val maxSpeedChange = if (desiredVelocity.x != 0f) {
                if (desiredVelocity.x.sign != velocity.x.sign) {
                    turnAcceleration * deltaTime.toFloat()
                } else {
                    properties.groundAccel * deltaTime.toFloat()
                }
            } else {
                backwardsAcceleration * deltaTime.toFloat()
            }
            velocity.x = velocity.x + (desiredVelocity.x - velocity.x) * maxSpeedChange

            if(player.onRoof){
                velocity.y = .00000001f
            }

            if (keyboard.isPressed(Key.LEFT_SHIFT)) {
                velocity.x *= 2
            }
            pos.add(velocity)
            transform.mutate()

            player.onGround = false
            player.wall = WallStatus.Off
            player.onRoof = false
        }
    }

    private fun Player.jump() {
        velocity.y = -properties.jumpSpeed
        if(!this.onGround) {
            if (this.wall == WallStatus.Right) {
                velocity.y = -properties.jumpSpeed
                velocity.x -= properties.jumpSpeed
            } else if (this.wall == WallStatus.Left) {
                velocity.y = -properties.jumpSpeed
                velocity.x += properties.jumpSpeed
            }
        }
    }

    private fun Player.canJump(): Boolean {
        return (this.onGround || this.wall != WallStatus.Off)
    }
}