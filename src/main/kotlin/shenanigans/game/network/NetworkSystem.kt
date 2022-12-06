package shenanigans.game.network

import com.google.common.collect.BiMap
import shenanigans.engine.ClientOnly
import shenanigans.engine.ecs.*
import kotlin.reflect.KClass

@ClientOnly
class Sendable : Component

class NetworkSystem : System{
    private val client : Client = Client

    private val ids : BiMap<Int, Int>
        get() {
            TODO()
        }

    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Sendable::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        for (entity in entities) {
            client.sendEntity(entity)
        }
    }
}