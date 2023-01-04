package shenanigans.game.network

import shenanigans.engine.ecs.*
import shenanigans.game.network.client.Client
import kotlin.reflect.KClass

class Synchronized : Component

class NetworkSystem : System{
    private val client : Client = Client

    private val clientIds : HashMap<EntityId, EntityId?> = HashMap()
    private val serverIds : HashMap<EntityId, EntityId> = HashMap()

    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Synchronized::class)
    }

    override fun execute(resources: ResourcesView, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        for (entity in entities) {
            if(!clientIds.containsKey(entity.id)) {
                client.createNetworkedEntity(entity)
                clientIds[entity.id] = null
                continue
            }

            if(clientIds[entity.id] == null) {
                continue
            }

            client.sendEntity(entity, clientIds[entity.id]!!)
        }
    }
}