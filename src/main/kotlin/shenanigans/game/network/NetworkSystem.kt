package shenanigans.game.network

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import kotlin.reflect.KClass

class Sendable : Component {}

class NetworkSystem : System{
    val client : Client = Client

    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Sendable::class)
    }

    override fun executePhysics(resources: ResourcesView, eventQueues: EventQueues, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        for (entity in entities) {
            client.sendEntity(entity)
        }
    }
}