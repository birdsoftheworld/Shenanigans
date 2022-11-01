package shenanigans.engine.scene

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.graphics.api.texture.TextureManager
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
                    Sprite(
                        TextureManager.createTexture("/sprite.png").getRegion(),
                        Vector2f(50f, 50f)
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

    /**
     * get a resource from the default resources
     */
    inline fun <reified T : Resource> getResource() : T {
        return resources.get()
    }

    /**
     * set a resource in the default resources
     */
    inline fun <reified T : Resource> setResource(resource: T) {
        resources.set(resource)
    }

    /**
     * run the default systems with default resources
     */
    fun runSystems() {
        systems.forEach {
            entities.runSystem(it, resources)
        }
    }

    /**
     * run the specified systems with the specified resources
     */
    fun runSystems(resources: Resources, systems: List<System>) {
        systems.forEach {
            entities.runSystem(it, resources)
        }
    }
}