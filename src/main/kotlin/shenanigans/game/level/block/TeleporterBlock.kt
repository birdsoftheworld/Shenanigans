package shenanigans.game.level.block

import org.joml.Vector3f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.Texture
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.util.Transform
import shenanigans.game.control.MouseDraggable
import kotlin.reflect.KClass

class TeleporterBlock : Block() {
    override val solid = false
    override val shape = SQUARE_BLOCK_SHAPE
    override val texture: Texture = TeleporterBlock.texture

    var targetPos = Vector3f(0f, 0f, 0f)

    companion object {
        val texture = TextureManager.createTexture(TextureKey("teleporterA"), "/teleporterA.png")
    }
}

class TeleporterSystem : System {// Implement
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(setOf(MouseDraggable::class, Transform::class))

        entities.forEach { entity ->

        }
    }
}