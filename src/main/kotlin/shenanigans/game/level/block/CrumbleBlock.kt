package shenanigans.game.level.block

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.physics.CollisionEvent
import shenanigans.engine.ecs.System
import shenanigans.engine.events.Event
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.graphics.api.texture.Texture
import shenanigans.engine.physics.Collider
import shenanigans.engine.timer.timeEventPhysics
import shenanigans.engine.util.shapes.Polygon
import java.util.*
import kotlin.reflect.KClass

class DoSomething(val target: UUID, val solid : Boolean) : Event
class HalfCrumble(val target: UUID) : Event


class CrumbleBlock : Block() {
    override var solid = true
    override val visualShape = SQUARE_BLOCK_SHAPE
    override val colliderShape: Polygon = SQUARE_BLOCK_SHAPE
    override var texture = CrumbleBlock.textureA

    var touched = false

    companion object {
        val textureA = TextureManager.createTexture(TextureKey("crumble"), "/crumble.png")
        val textureB = TextureManager.createTexture(TextureKey("halfCrumble"), "/halfCrumble.png")
        val textureC = TextureManager.createTexture(TextureKey("none"), "/none.png")
    }
}

class CrumbleSystem :System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(setOf(CrumbleBlock::class))

        entities.forEach { entity ->
            eventQueues.own.receive(CollisionEvent::class).filter {entity.id == it.with}.forEach { event ->
                if(entity.componentOpt<CrumbleBlock>() != null){
                    if(!entity.component<CrumbleBlock>().get().touched){
                        timeEventPhysics(1.0, DoSomething(entity.id, false))
                        timeEventPhysics(0.5, HalfCrumble(entity.id))
                        entity.component<CrumbleBlock>().get().touched = true
                    }
                }
            }
            eventQueues.own.receive(HalfCrumble::class).filter {entity.id == it.target}.forEach { event ->
                entity.component<CrumbleBlock>().get().texture = CrumbleBlock.textureB
                entity.component<Sprite>().get().sprite = entity.component<CrumbleBlock>().get().createSprite().sprite
            }
            eventQueues.own.receive(DoSomething::class).filter {entity.id == it.target}.forEach {event ->
                if(event.solid){//make solid
                    entity.component<Collider>().get().solid = true
                    entity.component<CrumbleBlock>().get().touched = false

                    entity.component<CrumbleBlock>().get().texture = CrumbleBlock.textureA
                    entity.component<Sprite>().get().sprite = entity.component<CrumbleBlock>().get().createSprite().sprite
                }
                else{//make gone
                    entity.component<Collider>().get().solid = false
                    timeEventPhysics(3.5, DoSomething(entity.id,true))
                    timeEventPhysics(3.0, HalfCrumble(entity.id))

                    entity.component<CrumbleBlock>().get().texture = CrumbleBlock.textureC
                    entity.component<Sprite>().get().sprite = entity.component<CrumbleBlock>().get().createSprite().sprite
                }
            }
            entity.component<CrumbleBlock>().mutate()
            entity.component<Collider>().mutate()
            entity.component<Sprite>().mutate()
        }
    }
}