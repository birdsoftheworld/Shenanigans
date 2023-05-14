package shenanigans.game.level.block

import org.joml.Vector2f
import org.joml.Vector2fc
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.physics.Time
import shenanigans.engine.util.Transform
import shenanigans.engine.util.shapes.Polygon
import kotlin.math.abs
import kotlin.reflect.KClass

enum class Direction(val vector: Vector2fc) {
    Up(
        Vector2f(0f, -1f)
    ), Right(
        Vector2f(1f, 0f)
    ), Down(
        Vector2f(0f, 1f)
    ), Left(
        Vector2f(-1f, 0f)
    );
    fun opposite(): Direction {
        return when(this) {
            Up -> Down
            Down -> Up
            Left -> Right
            Right -> Left
        }
    }
}

class OscillatingBlock(
    val distanceToOscillate: Float, var startPos: Vector2f?, var speed: Float, var dir: Direction = Direction.Right,
) : Block() {
    constructor() : this(128f, null, 100f)
    override val solid = true
    override val colliderShape: Polygon = SQUARE_BLOCK_SHAPE
    override val visualShape = SQUARE_BLOCK_SHAPE
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

    fun changeDirection() {
        dir = dir.opposite()
    }

    fun newStartPos(x: Float, y: Float) {
        this.startPos = Vector2f(x, y)
    }

    fun getMove(): Vector2f {
        return this.dir.vector.mul(this.speed, Vector2f())
    }

    companion object {
        val texture = TextureManager.createTexture(TextureKey("oscillator"), "/oscillator.png")
    }
}

class OscillatingBlocksSystem : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(setOf(Transform::class, OscillatingBlock::class))

        entities.forEach { entity ->
            val pos = entity.component<Transform>().get().position
            val oscillatingBlock = entity.component<OscillatingBlock>().get()
            val deltaTimeF = resources.get<Time>().deltaTime.toFloat()
            if(oscillatingBlock.startPos == null) {
                oscillatingBlock.newStartPos(pos.x, pos.y)
            }
            if (abs(pos.x - oscillatingBlock.startPos!!.x) > oscillatingBlock.distanceToOscillate || abs(pos.y - oscillatingBlock.startPos!!.y) > oscillatingBlock.distanceToOscillate) {
                oscillatingBlock.changeDirection()
            }
            val change = oscillatingBlock.getMove().mul(deltaTimeF)
            pos.add(change.x, change.y, 0f)
        }
    }
}