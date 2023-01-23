package shenanigans.game.network

import com.google.common.collect.BiMap
import shenanigans.engine.ClientOnly
import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntitiesView
import shenanigans.engine.ecs.EntityId
import shenanigans.engine.ecs.EntityView
import kotlin.reflect.KClass

class EntityPacket(serverTimeMillis: Int) : Packet(serverTimeMillis) {
    val entities: MutableMap<EntityId, MutableMap<KClass<out Component>, Component>> = mutableMapOf()

    constructor (entities: Sequence<EntityView>, clientIds: BiMap<EntityId, EntityId>, serverTimeMillis: Int) : this(
        serverTimeMillis
    ) {
        entities.forEach { entity ->
            entity.components.forEach() { componentMap ->
                if (!componentMap.value.component.javaClass.isAnnotationPresent(ClientOnly::class.java)) {
                    if (this.entities[clientIds[entity.id]!!] == null) {
                        this.entities[clientIds[entity.id]!!] = mutableMapOf()
                    }
                    this.entities[clientIds[entity.id]!!]!![componentMap.value.component::class] = componentMap.value.component
                }
            }
        }
    }

    constructor (entities: EntitiesView, serverTimeMillis: Int) : this(serverTimeMillis) {
        entities.forEach { entity ->
            entity.components.forEach() { componentMap ->
                if (!componentMap.value.component.javaClass.isAnnotationPresent(ClientOnly::class.java)) {
                    if (this.entities[entity.id] == null) {
                        this.entities[entity.id] = mutableMapOf()
                    }
                    this.entities[entity.id]!![componentMap.value.component::class] = componentMap.value.component
                }
            }
        }
    }
}

class EntityRegistrationPacket(val clientId: Int, serverTimeMillis: Int) : Packet(serverTimeMillis) {
    val components: MutableList<Component> = mutableListOf()
    var clientEntityId: EntityId = EntityId(-1)
    var serverEntityId: EntityId? = null

    constructor (entityView: EntityView, clientId: Int, serverTimeMillis: Int) : this(clientId, serverTimeMillis) {
        entityView.components.values.forEach {
            if (!it.component.javaClass.isAnnotationPresent(ClientOnly::class.java)) {
                components.add(it.component)
            }
        }
    }
}