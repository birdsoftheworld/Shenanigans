package shenanigans.engine.ecs

import kotlin.reflect.KClass

class Entities {
    private val entities: MutableList<StoredEntity> = arrayListOf()


    fun runSystem(system: System) {
        val query = system.query().toSet()

        val lifecycle = EntitiesLifecycle()
        system.execute(
            entities
                .asSequence()
                .withIndex()
                .filter {
                    it.value.components.keys.containsAll(query)
                }.map { EntityView(it.index, it.value) },
            lifecycle
        )
        lifecycle.finish(entities)
    }

}

internal data class StoredEntity(var version: Int, val components: Map<KClass<out Component>, Component>)

class EntityView internal constructor(
    val id: Int,
    private val entity: StoredEntity,

    val _components: Map<KClass<out Component>, Component> = entity.components,
) {
    inline fun <reified T : Component> component(): T {
        return componentOpt()!!
    }

    inline fun <reified T : Component> componentOpt(): T? {
        return _components[T::class] as T?
    }

    val version: Int
        get() = entity.version

    fun mutate() {
        entity.version++
    }
}

class EntitiesLifecycle internal constructor() {
    private val requests: MutableList<LifecycleRequest> = mutableListOf()

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

    internal fun finish(entities: MutableList<StoredEntity>) {
        requests.forEach { req ->
            when (req) {
                is LifecycleRequest.Add -> {
                    entities.add(StoredEntity(0, req.components.associateBy { it::class }))
                }

                is LifecycleRequest.Del -> {
                    entities.removeAt(req.id)
                }
            }
        }
    }
}
