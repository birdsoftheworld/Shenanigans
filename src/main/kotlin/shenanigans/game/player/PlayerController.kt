package shenanigans.game.player

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.util.Transform
import shenanigans.engine.window.Key
import shenanigans.engine.window.events.KeyboardState
import kotlin.math.abs
import kotlin.math.sign
import kotlin.reflect.KClass

data class Player(val xVel: Float, val xAccel: Float = .003f, val yAccel: Float = .003f, val xMax : Float = .5f, val yMax :  Float = .5f, val airDrag : Float = .997f) : Component

class PlayerController : System {
    val gravity : Float = .000f;
    var velocity = Vector2f()

    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Player::class, Transform::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val keyboard = resources.get<KeyboardState>()
        val deltaTime = resources.get<DeltaTime>().deltaTime
        entities.forEach { entity ->
            val xAccel = entity.component<Player>().get().xAccel
            val yAccel = entity.component<Player>().get().yAccel
            val xMax = entity.component<Player>().get().xMax
            val yMax = entity.component<Player>().get().yMax
            val airDrag = entity.component<Player>().get().airDrag
            velocity.add(Vector2f(0f,gravity))
            if (keyboard.isPressed(Key.W)) {
                velocity.mul(1f,0f)
                velocity.add(Vector2f(0f, -.1f))
            }
            if (keyboard.isPressed(Key.A)) {
                velocity.add(Vector2f(-xAccel, 0f))
            }
            else if (keyboard.isPressed(Key.D)) {
                velocity.add(Vector2f(xAccel, 0f))
            }
            else{
                if(abs(velocity.x) < .01){velocity.mul(Vector2f(0f,1f))}
                else{velocity.mul(Vector2f(airDrag,1f))}

            }
            if (keyboard.isPressed(Key.S)) {
                velocity.add(Vector2f(0f, yAccel))
            }

            if (velocity.length() > 0) {
                if(abs(velocity.x) > xMax){
                    velocity.x = (xMax*velocity.x.sign)
                }

                if(abs(velocity.y) > abs(yMax)){
                    velocity.y = (yMax*velocity.y.sign)
                }

                val transform = entity.component<Transform>()
                transform.get().position.add(velocity)
                transform.mutate()
            }
        }
    }
}