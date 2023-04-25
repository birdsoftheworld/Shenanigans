package shenanigans.game.level.block

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.util.Transform
import shenanigans.game.MousePlayer
import kotlin.math.abs
import kotlin.reflect.KClass

enum class Direction(sign: Int) {
    Up(0), Right(1), Down(2), Left(3)
}

class OscillatingBlock(
    val distanceToOscillate: Float, var startPos: Vector2f, var speed: Float, var dir: Direction = Direction.Right,
) : Block() {
    constructor() : this(50f, Vector2f(100f, 500f), .01f)

    override val solid = true
    override val shape = SQUARE_BLOCK_SHAPE
    override val texture = OscillatingBlock.texture

    fun rotate(clockwise: Boolean) {
        dir = if (clockwise) {
            when (dir) {
                Direction.Up -> Direction.Right
                Direction.Right -> Direction.Down
                Direction.Down -> Direction.Left
                Direction.Left -> Direction.Up
            }
        } else {
            when (dir) {
                Direction.Down -> Direction.Right
                Direction.Left -> Direction.Down
                Direction.Up -> Direction.Left
                Direction.Right -> Direction.Up
            }
        }
    }

    fun reset() {
        speed = abs(speed)
    }

    fun changeDirection() {
        speed *= -1
    }

    fun newStartPos(x: Float, y: Float) {
        this.startPos.set(x, y)
    }

    companion object {
        val texture = TextureManager.createTexture(TextureKey("arrow"), "/betterArrow.png")
    }
}

class OscillatingBlocksSystem : System {

    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(setOf(MousePlayer::class, Transform::class, OscillatingBlock::class))

        entities.forEach { entity ->
            val pos = entity.component<Transform>().get().position
            val oscillatingBlock = entity.component<OscillatingBlock>().get()
            val mousePlayer = entity.component<MousePlayer>().get()
            if (!mousePlayer.grabbed) {
                if (abs(pos.x - oscillatingBlock.startPos.x) > oscillatingBlock.distanceToOscillate || abs(pos.y - oscillatingBlock.startPos.y) > oscillatingBlock.distanceToOscillate) {
                    oscillatingBlock.changeDirection()
                }
                when (oscillatingBlock.dir) {
                    Direction.Up -> {
                        pos.y -= oscillatingBlock.speed
                    }

                    Direction.Right -> {
                        pos.x += oscillatingBlock.speed
                    }

                    Direction.Down -> {
                        pos.y += oscillatingBlock.speed
                    }

                    Direction.Left -> {
                        pos.x -= oscillatingBlock.speed
                    }
                }
            }
        }
    }
}