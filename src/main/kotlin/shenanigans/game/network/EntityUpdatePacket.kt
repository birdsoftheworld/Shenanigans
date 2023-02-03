package shenanigans.game.network

import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntitiesView
import shenanigans.engine.ecs.EntityView
import shenanigans.engine.events.Event
import java.util.*
import kotlin.reflect.KClass

class EntityUpdatePacket : Event {
    val entities: MutableMap<UUID, MutableMap<KClass<out Component>, Component>> = mutableMapOf()

    constructor (entities: Sequence<EntityView>) {
        entities.forEach { entity ->
            this.entities[entity.id] = mutableMapOf()
            entity.components.filter { !it.value.component.javaClass.isAnnotationPresent(ClientOnly::class.java) }.forEach() { componentMap ->
                this.entities[entity.id]!![componentMap.value.component::class] = componentMap.value.component
            }
        }
    }

    constructor (entities: EntitiesView) {
        entities.forEach { entity ->
            this.entities[entity.id] = mutableMapOf()
            entity.components.filter { !it.value.component.javaClass.isAnnotationPresent(ClientOnly::class.java) }.forEach() { componentMap ->
                this.entities[entity.id]!![componentMap.value.component::class] = componentMap.value.component
            }
        }
    }
}

class EntityRegistrationPacket() : Event {
    val components: MutableList<Component> = mutableListOf()

    constructor (entityView: EntityView) : this() {
        entityView.components.values.forEach {
            if (!it.component.javaClass.isAnnotationPresent(ClientOnly::class.java)) {
                components.add(it.component)
            }
        }
    }
}