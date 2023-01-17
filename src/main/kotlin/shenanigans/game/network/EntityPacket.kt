package shenanigans.game.network

import shenanigans.engine.ClientOnly
import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntityId
import shenanigans.engine.ecs.EntityView
import kotlin.reflect.KClass

class EntityPacket (val serverEntityId: EntityId, serverTimeMillis: Int): Packet(serverTimeMillis) {
    val components: MutableMap<KClass<out Component>, Component> = mutableMapOf()

    constructor (serverEntityId: EntityId, entityView: EntityView, serverTimeMillis : Int) : this(serverEntityId, serverTimeMillis) {
        entityView.components.values.forEach {
            if(!it.component.javaClass.isAnnotationPresent(ClientOnly::class.java)) {
                components[it.component::class] = it.component
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