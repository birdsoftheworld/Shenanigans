package shenanigans.engine.ecs

import kotlin.reflect.KClass

class Entities {
    internal val entities: EntityMap = EntityMap()

    fun runSystem(system: System, resourcesView: ResourcesView) {
        val query = system.query()

        val lifecycle = EntitiesLifecycle(entities)
        system.execute(
            resourcesView,
            entities
                .map
                .asSequence()
                .filter {
                    it.value.keys.containsAll(query as Collection<KClass<out Component>>)
                }.map { EntityView(it.key, it.value) },
            lifecycle
        )
        lifecycle.finish(entities)
    }
}

typealias Entity = Map<KClass<out Component>, StoredComponent>

internal class EntityMap {
    val map: MutableMap<EntityId, Entity> = mutableMapOf()
    private var lowestId: Int = 0

    fun reserveId(): EntityId {
        val reserved = EntityId(lowestId)
        lowestId++
        return reserved
    }

    fun add(id: EntityId, value: Entity) {
        if(map.containsKey(id)) {
            throw IllegalArgumentException("Cannot add entity id that already exists")
        }
        map[id] = value
    }

    fun add(value: Entity) {
        map[EntityId(lowestId)] = value
        lowestId++
    }
}

@JvmInline
value class EntityId(val id: Int)

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

class EntitiesLifecycle internal constructor(private val entities: EntityMap) {
    private val requests: MutableList<LifecycleRequest> = mutableListOf()

    sealed class LifecycleRequest {
        data class Add(val components: Sequence<Component>, val id: EntityId) : LifecycleRequest()
        data class Del(val id: EntityId) : LifecycleRequest()
    }

    fun add(components: Sequence<Component>): EntityId {
        val id = entities.reserveId()
        requests.add(LifecycleRequest.Add(components, id))
        return id
    }

    fun del(id: EntityId) {
        requests.add(LifecycleRequest.Del(id))
    }

    internal fun finish(entities: EntityMap) {
        requests.forEach { req ->
            when (req) {
                is LifecycleRequest.Add -> {
                    entities.add(req.id, req.components.map { StoredComponent(it) }.associateBy { it.component::class })
                }

                is LifecycleRequest.Del -> {
                    entities.map.remove(req.id)
                }
            }
        }
    }
}
