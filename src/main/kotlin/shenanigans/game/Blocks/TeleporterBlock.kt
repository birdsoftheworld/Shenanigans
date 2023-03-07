package shenanigans.game.Blocks

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.util.Transform
import kotlin.reflect.KClass

data class TeleporterBlock(val num : Int) : Component {
    var targetPos = Vector2f(0f,0f)
}

class TeleporterSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(TeleporterBlock::class, Transform::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        entities.forEach { entity ->
            val tpBlock = entity.component<TeleporterBlock>()
            if(tpBlock.get().num == 0) {
                entities.forEach { entity2 ->
                    if (entity2.component<TeleporterBlock>().get().num == 1) {
                        tpBlock.get().targetPos.set(entity2.component<Transform>().get().position)
                    }
                }
            }
        }
    }
}