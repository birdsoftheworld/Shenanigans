package shenanigans.engine.ecs

import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.EventQueues
import shenanigans.engine.term.Logger
import java.util.*
import kotlin.reflect.KClass

class Entities {
    internal val entities: HashMap<UUID, StoredEntity> = hashMapOf()

    fun <S : System, Q : EventQueue> runSystem(
        execute: S.(ResourcesView, EventQueues<Q>, (Iterable<KClass<out Component>>) -> QueryView, EntitiesLifecycle) -> Unit,
        system: S,
        resourcesView: ResourcesView,
        eventQueues: EventQueues<Q>,
    ) {
        val lifecycle = EntitiesLifecycle()
        execute(
            system,
            resourcesView,
            eventQueues,
            { query -> QueryView(this, query) },
            lifecycle
        )
        lifecycle.finish(this)
    }
}

data class StoredEntity(
    val components: Map<KClass<out Component>, StoredComponent>
)

data class StoredComponent(var component: Component, var version: Int = 0)

class EntityView internal constructor(
    entities: Entities,
    val id: UUID,
    @PublishedApi internal val entity: StoredEntity = entities.entities[id]!!
) {
    inline fun <reified T : Component> component(cl: KClass<out T>): ComponentView<T> {
        return componentOpt(cl)!!
    }

    inline fun <reified T : Component> componentOpt(cl: KClass<out T>): ComponentView<T>? {
        val stored = entity.components[cl]

        return if (stored != null) {
            ComponentView(stored)
        } else {
            null
        }
    }

    inline fun <reified T : Component> component(): ComponentView<T> {
        return component(T::class)
    }

    inline fun <reified T : Component> componentOpt(): ComponentView<T>? {
        return componentOpt(T::class)
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

    fun replace(new: T): T {
        val old = get()

        stored.component = new
        mutate()

        return old
    }

    operator fun component1(): T {
        return get()
    }

    operator fun component2(): Int {
        return version()
    }
}

class QueryView internal constructor(
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
                    entities.entities.remove(req.id)
                }
            }
        }
    }
}
