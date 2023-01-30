package shenanigans.engine.ecs

import java.util.UUID
import kotlin.reflect.KClass

class Entities {
    internal val entities: HashMap<UUID, StoredComponents> = hashMapOf()

    fun runSystem(system: System, resourcesView: ResourcesView) {
        val query = system.query()

        val lifecycle = EntitiesLifecycle()
        system.execute(
            resourcesView,
            EntitiesView(this, query),
            lifecycle
        )
        lifecycle.finish(this)
    }
}

internal typealias StoredComponents = Map<KClass<out Component>, StoredComponent>

data class StoredComponent(val component: Component, var version: Int = 0)

class EntityView internal constructor(
    val id: UUID,
    @PublishedApi internal val components: StoredComponents,
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
    private val entities: Entities,
    private val query: Iterable<KClass<out Component>>,
) : Sequence<EntityView> {
    fun get(id: UUID): EntityView? {
        return entities.entities[id]?.let { EntityView(id, it) }
    }

    override fun iterator(): Iterator<EntityView> {
        return entities.entities.filter { (_, components) ->
            components.keys.containsAll(query as Collection<KClass<out Component>>)
        }.map { (id, components) -> EntityView(id, components) }.iterator()
    }
}

class EntitiesLifecycle internal constructor() {
    private val requests: MutableList<LifecycleRequest> = mutableListOf()

    sealed class LifecycleRequest {
        data class Add(val id: UUID, val components: Sequence<Component>) : LifecycleRequest()
        data class Del(val id: UUID) : LifecycleRequest()
    }

    fun add(components: Sequence<Component>): UUID {
        val id = UUID.randomUUID()
        requests.add(LifecycleRequest.Add(id, components))
        return id
    }

    fun del(id: UUID) {
        requests.add(LifecycleRequest.Del(id))
    }

    internal fun finish(entities: Entities) {
        requests.forEach { req ->
            when (req) {
                is LifecycleRequest.Add -> {
                    entities.entities[req.id] =
                        req.components.map { StoredComponent(it) }.associateBy { it.component::class }
                }

                is LifecycleRequest.Del -> {
                    entities.entities.remove(req.id) ?: throw IllegalStateException("Entity ${req.id} not found")
                }
            }
        }
    }
}
