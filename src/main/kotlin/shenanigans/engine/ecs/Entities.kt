package shenanigans.engine.ecs

import kotlin.reflect.KClass

class Entities {
    private val entities: MutableList<Map<KClass<out Component>, StoredComponent>> = arrayListOf()


    fun runSystem(system: System, resources: Resources) {
        val query = system.query().toSet()

        val lifecycle = EntitiesLifecycle()
        system.execute(
            resources,
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

data class StoredComponent(val component: Component, var version: Int = 0)

class EntityView internal constructor(
    val id: Int,
    @PublishedApi internal val components: Map<KClass<out Component>, StoredComponent>,
) {
    inline fun <reified T : Component> component(): ComponentView<T> {
        return componentOpt()!!
    }

    inline fun <reified T : Component> componentOpt(): ComponentView<T>? {
        val stored = components[T::class]

        return if (stored !== null) {
            ComponentView(stored)
        } else {
            null
        }
    }
}

class ComponentView<T : Component>(private val stored: StoredComponent) {
    fun get(): T {
        return stored.component as T
    }

    fun version(): Int {
        return stored.version
    }

    fun mutate() {
        stored.version++
    }

    operator fun component1(): T {
        return get()
    }

    operator fun component2(): Int {
        return version()
    }
}

class EntitiesLifecycle internal constructor() {
    private val requests: MutableList<LifecycleRequest> = mutableListOf()

    sealed class LifecycleRequest {
        data class Add(val components: Sequence<Component>) : LifecycleRequest()
        data class Del(val id: Int) : LifecycleRequest()
    }

    fun add(components: Sequence<Component>) {
        requests.add(LifecycleRequest.Add(components))
    }

    fun del(id: Int) {
        requests.add(LifecycleRequest.Del(id))
    }

    internal fun finish(entities: MutableList<Map<KClass<out Component>, StoredComponent>>) {
        requests.forEach { req ->
            when (req) {
                is LifecycleRequest.Add -> {
                    entities.add(req.components.map { StoredComponent(it) }.associateBy { it.component::class })
                }

                is LifecycleRequest.Del -> {
                    entities.removeAt(req.id)
                }
            }
        }
    }
}
