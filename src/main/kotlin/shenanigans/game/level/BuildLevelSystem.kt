package shenanigans.game.level

import org.joml.Vector3f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.util.Transform
import shenanigans.game.level.block.*
import shenanigans.game.network.Synchronized
import kotlin.reflect.KClass

object BuildLevelSystem : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        fun line(x : Int, y : Int, pos : Vector3f){
            for (c in 1..x) {
                for (r in 1..y) {
                    insertBlock(
                        lifecycle,
                        NormalBlock(),
                        Transform(Vector3f(pos.x + c * (GRID_SIZE / 2), pos.y + r * (GRID_SIZE / 2), 50f)),
                        modifiable = false
                    )
                }
            }
        }

        fun box(x : Int, y : Int, pos : Vector3f) {
            line(x, 1, pos)
            pos.y += y * (GRID_SIZE / 2)
            line(x, 1, pos)
            pos.y -= y * (GRID_SIZE / 2)
            line(1, y, pos)
            pos.x += x * (GRID_SIZE / 2)
            line(1, y, pos)
        }

        //Player Respawn Block
        insertBlock(
            lifecycle,
            RespawnBlock(),
            Transform(Vector3f(96f, 608f, 100f)),
            modifiable = false
        )

        box(80,20, Vector3f(0f, 0f, .9f))
    }
}

fun roundBlockPosition(position: Vector3f): Vector3f {
    return position.sub(GRID_SIZE / 2, GRID_SIZE / 2, 0f).mul(1 / GRID_SIZE).round().mul(GRID_SIZE)
}

fun insertBlock(lifecycle: EntitiesLifecycle, block: Block, transform: Transform, modifiable: Boolean) {
    val set = mutableSetOf<Component>(Synchronized())

    if(modifiable) {
        set.add(Modifiable)
    }

    transform.position = roundBlockPosition(transform.position)
    val components = block.toComponents(transform).plus(
        set
    )

    lifecycle.add(components)
}
