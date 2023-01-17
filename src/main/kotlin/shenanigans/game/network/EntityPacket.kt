package shenanigans.game.network

import shenanigans.engine.ClientOnly
import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntitiesView
import shenanigans.engine.ecs.EntityId
import shenanigans.engine.ecs.EntityView
import kotlin.reflect.KClass

class EntityPacket (serverTimeMillis: Int): Packet(serverTimeMillis) {
    val entities: MutableMap<EntityId, MutableMap<KClass<out Component>, Component>> = mutableMapOf()

    constructor (entities: EntitiesView, clientIds : HashMap<EntityId, EntityId?>, serverTimeMillis : Int) : this(serverTimeMillis) {
        entities.forEach { entity ->
            entity.components.forEach() {component ->
                if (!component.javaClass.isAnnotationPresent(ClientOnly::class.java)) {
                    entities[serverEntityId]?.set(it.component::class, it.component)
                }
            }
        }
    }
}

class EntityRegistrationPacket (val clientId: Int, serverTimeMillis : Int): Packet(serverTimeMillis) {
    val components: MutableList<Component> = mutableListOf()
    var clientEntityId: EntityId = EntityId(-1)
    var serverEntityId: EntityId? = null

    constructor (entityView: EntityView, clientId: Int, serverTimeMillis : Int) : this(clientId, serverTimeMillis){
        entityView.components.values.forEach {
            if(!it.component.javaClass.isAnnotationPresent(ClientOnly::class.java)) {
                components.add(it.component)
            }
        }
    }
}