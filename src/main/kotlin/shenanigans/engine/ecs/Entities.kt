package shenanigans.engine.ecs

import kotlin.reflect.KClass

class Entities {
    private val entities: MutableList<Pair<EntityId, Map<KClass<out Component>, StoredComponent>>> = arrayListOf()
    private var nextId: Int = 0

    fun runSystem(system: System, resourcesView: ResourcesView) {
        val query = system.query()

        val lifecycle = EntitiesLifecycle(nextId)
        system.execute(
            resourcesView,
            entities
                .asSequence()
                .withIndex()
                .filter { (_, value) ->
                    val (_, components) = value
                    components.keys.containsAll(query as Collection<KClass<out Component>>)
                }.map { (_, value) ->
                    val (id, components) = value
                    EntityView(id, components)
                },
            lifecycle
        )
        lifecycle.finish(entities)
        nextId = lifecycle.nextId
    }
}

@JvmInline
value class EntityId(val number: Int)

data class StoredComponent(val component: Component, var version: Int = 0)

class EntityView internal constructor(
    val id: EntityId,
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

class EntitiesLifecycle internal constructor(var nextId: Int) {
    private val requests: MutableList<LifecycleRequest> = mutableListOf()

    sealed class LifecycleRequest {
        data class Add(val id: EntityId, val components: Sequence<Component>) : LifecycleRequest()
        data class Del(val id: EntityId) : LifecycleRequest()
    }

    fun add(components: Sequence<Component>): EntityId {
        val id = genId()
        requests.add(LifecycleRequest.Add(id, components))
        return id
    }

    fun del(id: EntityId) {
        requests.add(LifecycleRequest.Del(id))
    }

    internal fun finish(entities: MutableList<Pair<EntityId, Map<KClass<out Component>, StoredComponent>>>) {
        requests.forEach { req ->
            when (req) {
                is LifecycleRequest.Add -> {
                    entities.add(
                        Pair(
                            req.id,
                            req.components.map { StoredComponent(it) }.associateBy { it.component::class })
                    )
                }

                is LifecycleRequest.Del -> {
                    val idx = entities.binarySearch { (id, _) -> id.number - req.id.number }
                    entities.removeAt(idx)
                }
            }
        }
    }

    private fun genId(): EntityId {
        val id = nextId
        nextId++
        return EntityId(id)
    }
}
