package shenanigans.game.level.block

import org.joml.Vector3f
import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.graphics.api.texture.Texture
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import shenanigans.engine.util.shapes.Polygon
import shenanigans.engine.util.shapes.Rectangle

object Modifiable : Component

sealed class Block : Component {
    abstract val solid: Boolean
    abstract val visualShape: Rectangle
    abstract val colliderShape: Polygon
    abstract val texture: Texture

    fun toComponents(pos: Vector3f): Sequence<Component> {
        return sequenceOf(
            this,
            Transform(pos),
            Collider(colliderShape, true, solid, false),
            this.createSprite()
        )
    }

    fun createSprite(): Sprite {
        return Sprite(texture.getRegion(), visualShape)
    }

    companion object {
        fun initAll() {
            GoalBlock
            NormalBlock
            OscillatingBlock
            RespawnBlock
            IceBlock
            SpikeBlock
            StickyBlock
            TeleporterBlock
            TrampolineBlock
            AccelerationBlock
        }
    }
}

const val GRID_SIZE = 64f

//Textures
val SQUARE_BLOCK_SHAPE: Rectangle = Rectangle(GRID_SIZE, GRID_SIZE)

private const val smallerSize = GRID_SIZE * .9f
val SLIGHTLY_SMALLER_SQUARE = Rectangle(smallerSize, smallerSize).offset((GRID_SIZE - smallerSize) / 2)