package shenanigans.engine.ecs

import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.EventQueues
import java.util.*
import kotlin.reflect.KClass

class Entities {
    internal val entities: HashMap<UUID, StoredEntity> = hashMapOf()

    fun <S : System, Q : EventQueue> runSystem(
        execute: S.(ResourcesView, EventQueues<Q>, EntitiesView, EntitiesLifecycle) -> Unit,
        system: S,
        resourcesView: ResourcesView,
        eventQueues: EventQueues<Q>,
    ) {
        val query = system.query()

        val lifecycle = EntitiesLifecycle()
        execute(
            system,
            resourcesView,
            eventQueues,
            EntitiesView(this, query),
            lifecycle
        )
        lifecycle.finish(this)
    }
}

data class StoredEntity(
    val components: Map<KClass<out Component>, StoredComponent>
)

data class StoredComponent(val component: Component, var version: Int = 0)

class EntityView internal constructor(
    entities: Entities,
    val id: UUID,
    @PublishedApi internal val entity: StoredEntity = entities.entities[id]!!
) {

    inline fun <reified T : Component> component(): ComponentView<T> {
        return componentOpt()!!
    }

    inline fun <reified T : Component> componentOpt(): ComponentView<T>? {
        val stored = entity.components[T::class]

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
    operator fun get(id: UUID): EntityView? {
        return entities.entities[id]?.let { EntityView(entities, id) }
    }

    override fun iterator(): Iterator<EntityView> {
        return entities.entities.filter { (_, entity) ->
            entity.components.keys.containsAll(query as Collection<KClass<out Component>>)
        }.map { (id, _) -> EntityView(entities, id) }.iterator()
    }
}

class EntitiesLifecycle internal constructor() {
    private val requests: MutableList<LifecycleRequest> = mutableListOf()

    sealed class LifecycleRequest {
        data class Add(val id: UUID, val components: Sequence<Component>) : LifecycleRequest()
        data class Del(val id: UUID) : LifecycleRequest()
    }

    fun add(components: Sequence<Component>, id: UUID = UUID.randomUUID()): UUID {
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
                    entities.entities[req.id] = StoredEntity(
                        components = req.components.map { StoredComponent(it) }.associateBy { it.component::class },
                    )
                }

                is LifecycleRequest.Del -> {
                    entities.entities.remove(req.id) ?: throw IllegalStateException("Entity ${req.id} not found")
                }
            }
        }
    }
}
