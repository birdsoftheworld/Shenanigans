package shenanigans.game.network

import shenanigans.engine.ecs.EntitiesView
import shenanigans.engine.ecs.EntityView
import shenanigans.engine.events.Event
import shenanigans.engine.util.Transform
import java.util.*

class EntityMovementPacket : Event {
    val entities: MutableMap<UUID, Transform> = mutableMapOf()

    constructor (entities: Sequence<EntityView>) {
        entities.forEach { entity ->
            this.entities[entity.id] = entity.component<Transform>().get()
        }
    }

    constructor (entities: EntitiesView) {
        entities.forEach { entity ->
            this.entities[entity.id] = entity.component<Transform>().get()
        }
    }
}

class EntityRegistrationPacket(val entity: EntityView) : Event