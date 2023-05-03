package shenanigans.demo.chess

import org.joml.Vector2f
import shenanigans.engine.ClientEngine
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.net.Network
import shenanigans.engine.net.NullNetwork
import shenanigans.engine.scene.Scene
import shenanigans.engine.util.Transform
import shenanigans.engine.util.shapes.Polygon
import kotlin.reflect.KClass

fun main() {
    val engine = ClientEngine(Scene(), Network(NullNetwork))

    engine.runPhysicsOnce(AddTiles())

    engine.run()
}

class AddTiles : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val tileSize = 80f

        for (i in 0..7) {
            for (j in 0..7) {
                lifecycle.add(
                    sequenceOf(
                        Transform(Vector2f(i * tileSize, j * tileSize)),
                        Shape(
                            Polygon(
                                arrayOf(
                                    Vector2f(0f, 0f),
                                    Vector2f(0f, tileSize),
                                    Vector2f(tileSize, tileSize),
                                    Vector2f(tileSize, 0f),
                                )
                            ),
                            if ((i + j) % 2 == 0) {
                                Color(1f, 1f, 1f)
                            } else {
                                Color(0f, 0f, 0f)
                            }
                        )
                    )
                )
            }
        }
    }
}