package shenanigans.game.network

import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntitiesView
import shenanigans.engine.ecs.EntityView
import shenanigans.engine.events.Event
import shenanigans.engine.util.Transform
import java.util.*
import kotlin.reflect.KClass

class EntityMovementPacket(val entities: Map<UUID, Transform>): Event {
    constructor (entities: Sequence<EntityView>) : this(entities.map { it.id to it.component<Transform>().get()}.toMap())

    constructor (entities: EntitiesView) : this( entities.iterator().asSequence().map { it.id to it.component<Transform>().get()}.toMap())
}

class EntityRegistrationPacket(val id: UUID, val entity: Map<KClass<out Component>, Component>) : Event {
    constructor(entityView: EntityView) :
            this(entityView.id, entityView.entity.components.mapValues { it.value.component }.filterValues {!it.javaClass.isAnnotationPresent(ClientOnly::class.java)})
}