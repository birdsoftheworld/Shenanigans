package shenanigans.engine.scene

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.graphics.Shape
import shenanigans.engine.physics.Collider
import shenanigans.engine.physics.CollisionSystem
import shenanigans.engine.resources.DeltaTime
import shenanigans.engine.util.Transform
import kotlin.reflect.KClass

class Scene {
    private val entities : Entities = Entities()
    private val systems = mutableListOf<System>()

    internal class TestSystem : System {
        override fun query(): Iterable<KClass<out Component>> {
            return listOf()
        }

        override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
            val shape = Shape(arrayOf(Vector2f(-50f, -50f), Vector2f(50f, -50f), Vector2f(50f, 50f),Vector2f(-50f, 50f)))
            lifecycle.add(listOf(
                Transform(),
                shape,
                Collider(shape, false)
            ))
            lifecycle.add(listOf(
                Transform(Vector2f(10f, 10f)),
                shape,
                Collider(shape, false)
            ))
        }
    }

    init {
        entities.runSystem(TestSystem(), Resources())
        systems.add(CollisionSystem())
    }

    fun runSystems(deltaTime : DeltaTime) {
        val resources = Resources()
        resources.set(deltaTime)

        systems.forEach() {
            entities.runSystem(it, resources)
        }
    }

    fun delete() {

    }
}