package shenanigans.game.network

import shenanigans.engine.ClientOnly
import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntityView
import kotlin.reflect.KClass

class EntityPacket (val id: Int, entityView: EntityView, serverTimeMillis : Int): Packet(serverTimeMillis) {
    val components: MutableMap<KClass<out Component>, Component> = mutableMapOf()

    init {
        entityView.components.values.forEach {
            if(!it.component.javaClass.isAnnotationPresent(ClientOnly::class.java)) {
                components[it.component::class] = it.component
            }
        }
    }
}

class EntityRegistrationPacket(entityView: EntityView, val clientId: Int, serverTimeMillis : Int): Packet(serverTimeMillis) {
    val components: MutableList<Component> = mutableListOf()
    val clientEntityId: Int
    var serverEntityId: Int? = null

    init {
        entityView.components.values.forEach {
            if(!it.component.javaClass.isAnnotationPresent(ClientOnly::class.java)) {
                components.add(it.component)
            }
        }

        clientEntityId = entityView.id
    }
}