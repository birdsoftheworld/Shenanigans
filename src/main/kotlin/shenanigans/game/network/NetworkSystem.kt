package shenanigans.game.network

import shenanigans.engine.ecs.*
import kotlin.reflect.KClass

class Sendable : Component {}

class NetworkSystem : System{
    val client : Client = Client

    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Sendable::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        for (entity in entities) {
            client.sendEntity(entity)
        }
    }
}