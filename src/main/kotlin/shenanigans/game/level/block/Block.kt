package shenanigans.game.level.block

import org.joml.Vector3f
import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.graphics.api.texture.Texture
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import shenanigans.engine.util.shapes.Rectangle

sealed class Block : Component {
    abstract val solid: Boolean
    abstract val shape: Rectangle
    abstract val texture: Texture

    fun toComponents(pos: Vector3f): Sequence<Component> {
        return sequenceOf(
            this,
            Transform(pos),
            Collider(shape, true, solid, false),
            Sprite(texture.getRegion(), shape)
        )
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
        }
    }
}


//Textures
val SQUARE_BLOCK_SHAPE: Rectangle = Rectangle(64f, 64f)
