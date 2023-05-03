package shenanigans.game.level

import org.joml.Vector3f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.util.shapes.Rectangle
import shenanigans.game.level.block.*
import kotlin.reflect.KClass

object BuildLevelSystem : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        //Shapes
        val singleShape = Rectangle(64f, 64f)

        fun line(x : Int, y : Int, pos : Vector3f){
            for (c in 1..x) {
                for (r in 1..y) {
                    insertBlock(
                        lifecycle,
                        NormalBlock(singleShape),
                        Vector3f(pos.x + c * 32, pos.y + r * 32, 100f)
                    )
                }
            }
        }

        fun box(x : Int, y : Int, pos : Vector3f){
            line(x, 1, pos)
            pos.y+=y*32
            line(x, 1, pos)
            pos.y-=y*32
            line(1, y, pos)
            pos.x+=x*32
            line(1, y, pos)
        }

        //Player Respawn Block
        insertBlock(
            lifecycle,
            RespawnBlock(),
            Vector3f(96f, 608f, 100f)
        )

        box(80,20, Vector3f(0f, 0f, .9f))
    }
}