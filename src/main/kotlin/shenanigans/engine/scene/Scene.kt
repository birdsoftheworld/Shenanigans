package shenanigans.engine.scene

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.graphics.Color
import shenanigans.engine.graphics.Shape
import shenanigans.engine.util.Transform
import kotlin.reflect.KClass

class Scene {
    private val entities = Entities()
    private val systems = mutableListOf<System>()

    val resources = Resources()

    private inner class InitSystem : System {
        override fun query(): Iterable<KClass<out Component>> {
            return emptySet()
        }

        override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
            lifecycle.add(
                setOf(
                    Shape(
                        arrayOf(
                            Vector2f(0f, 0f),
                            Vector2f(0f, 100f),
                            Vector2f(100f, 100f),
                            Vector2f(100f, 0f)
                        ),
                        Color(0f, 1f, 1f)
                    ),
                    Transform(
                        Vector2f(100f, 100f),
                        0f,
                        Vector2f(1f, 1f)
                    )
                )
            )
        }
    }

    init {
        entities.runSystem(InitSystem(), resources)
    }

    inline fun <reified T : Resource> getResource() : T {
        return resources.get()
    }

    inline fun <reified T : Resource> setResource(resource: T) {
        resources.set(resource)
    }

    fun runSystems() {
        systems.forEach {
            entities.runSystem(it, resources)
        }
    }

    fun runSystems(resources: Resources, systems: List<System>) {
        systems.forEach {
            entities.runSystem(it, resources)
        }
    }
}