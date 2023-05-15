package shenanigans.game.level

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.game.level.block.slipperyDecelerationMultiplier
import shenanigans.game.level.block.slipperyMovementMultiplier
import shenanigans.game.level.block.slipperyTurnSpeedMultiplier
import shenanigans.game.level.component.MODIFIERS
import kotlin.reflect.KClass

object RegistrySystem : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        MODIFIERS["ice"] = {
            it.maxSpeed *= slipperyMovementMultiplier
            it.maxTurnSpeed *= slipperyTurnSpeedMultiplier
            it.maxDeceleration *= slipperyDecelerationMultiplier
            it.maxAirTurnSpeed *= 0
        }
    }
}