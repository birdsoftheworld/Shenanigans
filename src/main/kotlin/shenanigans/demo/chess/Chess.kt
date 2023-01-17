package shenanigans.demo.chess

import org.joml.Vector2f
import shenanigans.engine.ClientEngine
import shenanigans.engine.ecs.*
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.scene.Scene
import shenanigans.engine.util.Transform
import kotlin.reflect.KClass

fun main() {
    ClientEngine(makeScene()).run()
}

fun makeScene(): Scene {
    val scene = Scene()

    scene.runSystems(ResourcesView(), listOf(AddTiles()))

    return scene
}

class AddTiles : System {
    override fun query(): Iterable<KClass<out Component>> {
        return emptySet()
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val tileSize = 20f

        for (i in 0..7) {
            for (j in 0..7) {
                lifecycle.add(
                    sequenceOf(
                        Transform(Vector2f(i * tileSize, j * tileSize)),
                        Shape(
                            arrayOf(
                                Vector2f(0f, 0f),
                                Vector2f(0f, tileSize),
                                Vector2f(tileSize, tileSize),
                                Vector2f(tileSize, 0f),
                            ),
                            if ((i + j) % 2 == 0) {
                                Color(0f, 0f, 0f)
                            } else {
                                Color(1f, 1f, 1f)
                            }
                        )
                    )
                )
            }
        }
    }
}