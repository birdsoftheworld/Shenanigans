package shenanigans.game.level

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenangians.game.level.block.Block
import shenangians.game.level.block.ModifierBlock
import shenanigans.game.level.component.MODIFIERS
import kotlin.reflect.KClass

object RegistrySystem : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        // will also register all block textures
        val all = Block.all()

        for (block in all) {
            if (block is ModifierBlock) {
                MODIFIERS[block.modifier.first] = block.modifier.second
            }
        }
    }
}