package shenanigans.engine.ecs

import org.joml.Vector2d
import org.joml.Vector2f
import shenanigans.engine.ecs.components.Collider
import shenanigans.engine.ecs.components.Shape
import shenanigans.engine.ecs.components.Transform
import kotlin.reflect.KClass

class Entities {
    private val entities: MutableList<Map<KClass<out Component>, Component>> = arrayListOf()

    fun runSystem(system: System) {
        val query = system.query().toSet()

        val lifecycle = EntitiesLifecycle()
        system.execute(
            entities
                .asSequence()
                .withIndex()
                .filter {
                    it.value.keys.containsAll(query)
                }.map { EntityView(it.index, it.value) },
            lifecycle
        )
        lifecycle.finish(entities)
    }
}

class EntityView internal constructor(val id: Int, val _map: Map<KClass<out Component>, Component>) {
    inline fun <reified T : Component> component() {
        _map[T::class]
    }

    init {
        val map = mutableMapOf<KClass<out Component>, Component>()
        map[Transform::class] = Transform(Vector2f(10f, 10f), 0f, Vector2f(1f, 1f))
        map[Shape::class] = Shape(arrayOf(Vector2f(0f, 0f), Vector2f(10f, 0f), Vector2f(10f, 10f), Vector2f(0f, 10f)))
        map[Collider::class] = Collider(map[Shape::class] as Shape, static = false, triggerCollider = false)
        entities.add(map)
    }
}

class EntitiesLifecycle internal constructor() {
    private val requests: MutableList<LifecycleRequest> = mutableListOf();

    sealed class LifecycleRequest {
        data class Add(val components: Iterable<Component>) : LifecycleRequest()
        data class Del(val id: Int) : LifecycleRequest()
    }

    fun add(components: Iterable<Component>) {
        requests.add(LifecycleRequest.Add(components))
    }

    fun del(id: Int) {
        requests.add(LifecycleRequest.Del(id))
    }

    internal fun finish(entities: MutableList<Map<KClass<out Component>, Component>>) {
        requests.forEach { req ->
            when (req) {
                is LifecycleRequest.Add -> {
                    entities.add(req.components.associateBy { it::class })
                }

                is LifecycleRequest.Del -> {
                    entities.removeAt(req.id)
                }
            }
        }
    }
}
