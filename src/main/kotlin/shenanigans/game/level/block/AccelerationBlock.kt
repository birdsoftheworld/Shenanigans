package shenanigans.game.level.block

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.physics.CollisionEvent
import shenanigans.engine.util.shapes.Polygon
import kotlin.reflect.KClass

class AccelerationBlock : Block() {
    override val solid = false
    override val visualShape = SQUARE_BLOCK_SHAPE
    override val colliderShape: Polygon = SQUARE_BLOCK_SHAPE
    override val texture = AccelerationBlock.texture
    var used = false


    companion object {
        val texture = TextureManager.createTexture(TextureKey("acceleration"), "/acceleration.png")
    }
}

class AccelerationSystem : System {
    // Implement
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(setOf(AccelerationBlock::class))

        entities.forEach { entity ->
            if (!eventQueues.own.receive(CollisionEvent::class).filter { entity.id == it.with }.any()) {
                if (entity.component<AccelerationBlock>().get().used) {
                    entity.component<AccelerationBlock>().get().used = false
                }
            }
        }
    }
}