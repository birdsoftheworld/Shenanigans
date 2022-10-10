package shenanigans.engine.ecs

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
    inline fun <reified T : Component> component(): T {
        return _map[T::class] as T
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
