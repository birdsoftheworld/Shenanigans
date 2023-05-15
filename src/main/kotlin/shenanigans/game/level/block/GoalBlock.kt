package shenanigans.game.level.block

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.physics.Collider
import shenanigans.engine.physics.CollisionEvent
import shenanigans.engine.util.shapes.Polygon
import shenanigans.game.player.Player
import kotlin.reflect.KClass

class GoalBlock : Block() {
    override val solid = false
    override val visualShape = SQUARE_BLOCK_SHAPE
    override val colliderShape: Polygon = SLIGHTLY_SMALLER_SQUARE
    override val texture = GoalBlock.texture

    companion object {
        val texture = TextureManager.createTexture(TextureKey("goal"), "/goal.png")
    }
}

class GoalSystem : System {

    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(setOf(Collider::class))

        eventQueues.own.receive(CollisionEvent::class)
            .filter {
                    entities[it.target]!!.componentOpt<Player>() != null &&
                    entities[it.with]!!.componentOpt<GoalBlock>() != null
                    ||
                    entities[it.target]!!.componentOpt<Player>() != null &&
                    entities[it.target]!!.componentOpt<GoalBlock>() != null
            }
            .forEach { _ ->
                entities.filter { it.componentOpt<Modifiable>() != null }.forEach {
                    lifecycle.del(it.id)
                }
            }
    }
}