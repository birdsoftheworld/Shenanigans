package shenanigans.game.network

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import shenanigans.engine.ClientOnly
import shenanigans.engine.ecs.*
import shenanigans.game.network.client.Client
import kotlin.reflect.KClass

@ClientOnly
class Sendable : Component

class NetworkSystem : System{
    private val client : Client = Client

    private val ids : BiMap<Int, Int> = HashBiMap.create()

    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Sendable::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        for (entity in entities) {
            if(!ids.containsKey(entity.id)) {
                continue
            }

            client.sendEntity(entity)
        }
    }
}