package shenanigans.game.level.block

import org.joml.Vector3f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.api.texture.Texture
import shenanigans.engine.util.Transform
import shenanigans.game.control.MouseDraggable
import kotlin.reflect.KClass

data class TeleporterBlock(val num : Int) : Block() {
    override val solid = false
    override val shape = SQUARE_BLOCK_SHAPE
    override val texture: Texture = NULL_TEXTURE

    var targetPos = Vector3f(0f,0f,0f)
}

class TeleporterSystem : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(setOf(MouseDraggable::class, Transform::class))

        entities.forEach { entity ->
            val tpBlock = entity.component<TeleporterBlock>()
            if(tpBlock.get().num == 0) {
                entities.forEach { entity2 ->
                    if (entity2.component<TeleporterBlock>().get().num == 1) {
                        tpBlock.get().targetPos = entity2.component<Transform>().get().position
                        tpBlock.mutate()
                    }
                }
            }
        }
    }
}