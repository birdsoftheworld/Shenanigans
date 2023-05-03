package shenanigans.game.network

import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntityView
import shenanigans.engine.events.Event
import shenanigans.engine.net.ClientOnly
import java.util.*
import kotlin.reflect.KClass

class EntityUpdatePacket(val entities: Map<UUID, Map<KClass<out Component>, Component>>): Event

class EntityRegistrationPacket(val id: UUID, val entity: Map<KClass<out Component>, Component>) : Event {
    constructor(entityView: EntityView) :
            this(entityView.id, entityView.entity.components.mapValues { it.value.component }.filterValues {!it.javaClass.isAnnotationPresent(
                ClientOnly::class.java)})
}

class EntityDeRegistrationPacket(val id: UUID) : Event