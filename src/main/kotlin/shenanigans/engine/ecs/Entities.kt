package shenanigans.engine.ecs

import kotlin.reflect.KClass

class Entities {
    private val entities: MutableList<StoredEntity> = arrayListOf()
    private var nextId: Int = 0

    fun runSystem(system: System, resourcesView: ResourcesView) {
        val query = system.query()

        val lifecycle = EntitiesLifecycle(nextId)
        system.execute(
            resourcesView,
            EntitiesView(query, entities),
            lifecycle
        )
        lifecycle.finish(entities)
        nextId = lifecycle.nextId
    }
}

typealias StoredEntity = Pair<EntityId, Map<KClass<out Component>, StoredComponent>>

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

class EntitiesView internal constructor(
    private val query: Iterable<KClass<out Component>>,
    private val entities: List<StoredEntity>,
) : Sequence<EntityView> {
    operator fun get(id: EntityId): EntityView? {
        val idx = entities.binarySearch { (sid, _) -> sid.number - id.number }
        val (foundId, components) = entities[idx]

        return if (foundId == id) {
            EntityView(foundId, components)
        } else {
            null
        }
    }

    override fun iterator(): Iterator<EntityView> {
        return entities.filter { (_, components) ->
            components.keys.containsAll(query as Collection<KClass<out Component>>)
        }.map { (id, components) -> EntityView(id, components) }.iterator()
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

    internal fun finish(entities: MutableList<StoredEntity>) {
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

                    if (entities[idx].first == req.id) {
                        entities.removeAt(idx)
                    } else {
                        throw IllegalStateException("Entity ${req.id} not found")
                    }
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
