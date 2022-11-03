package shenanigans.engine.scene

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.Shape
import shenanigans.engine.physics.Collider
import shenanigans.engine.physics.CollisionSystem
import shenanigans.engine.input.Key
import shenanigans.engine.input.Movable
import shenanigans.engine.resources.DeltaTime
import shenanigans.engine.resources.KeyboardInput
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
            val shape = Shape(
                arrayOf(
                    Vector2f(0f, 100f),
                    Vector2f(100f, 100f),
                    Vector2f(100f, 0f),
                    Vector2f(0f, 0f)
                ),
                Color(0f, 0.5f, 0.5f)
            )
            lifecycle.add(
                setOf(
                    shape,
                    Transform(Vector2f(100f, 100f)),
                    Collider(
                        shape,
                        false
                    )
                )
            )
            lifecycle.add(
                setOf(
                    shape,
                    Transform(Vector2f(100f, 100f)),
                    Collider(
                        shape,
                        false
                    )
                )
            )

            lifecycle.add(
                setOf(
                    shape,
                    Transform(Vector2f(140f, 150f)),
                    Collider(
                        shape,
                        false
                    ),
                    Movable()
                )
            )
        }
    }

    // fixme
    private inner class DontLetThisGetPushedToMaster : System {
        override fun query(): Iterable<KClass<out Component>> {
            return setOf(Transform::class, Movable::class)
        }

        override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
            val dt = resources.get<DeltaTime>().deltaTime

            val keyboard = resources.get<KeyboardInput>()
            val w = keyboard.isDown(Key.W)
            val a = keyboard.isDown(Key.A)
            val s = keyboard.isDown(Key.S)
            val d = keyboard.isDown(Key.D)

            var x = 0f
            var y = 0f

            val speed = 300f

            if(w) {
                y -= dt.toFloat() * speed
            }
            if(a) {
                x -= dt.toFloat() * speed
            }
            if(s) {
                y += dt.toFloat() * speed
            }
            if(d) {
                x += dt.toFloat() * speed
            }

            val combined = Vector2f(x, y)

            for (entity in entities) {
                val trans = entity.component<Transform>().get()
                trans.position.add(combined)
            }
        }
    }

    init {
        entities.runSystem(InitSystem(), resources)

        systems.add(CollisionSystem())
        systems.add(DontLetThisGetPushedToMaster())
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