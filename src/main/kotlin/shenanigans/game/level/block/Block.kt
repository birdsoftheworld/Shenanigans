package shenanigans.game.level.block

import org.joml.Vector2fc
import org.joml.Vector3f
import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.graphics.api.texture.Texture
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import shenanigans.engine.util.shapes.Polygon
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
}


//Textures
val SQUARE_BLOCK_SHAPE: Rectangle = Rectangle(50f, 50f)
val NULL_TEXTURE: Texture = TextureManager.createTexture(TextureKey("null"), "/null.png")
val NORMAL_TEXTURE: Texture = TextureManager.createTexture(TextureKey("normal"), "/normalSprite.png")
val STICKY_TEXTURE: Texture = TextureManager.createTexture(TextureKey("sticky"), "/sticky.png")
val ICE_TEXTURE: Texture = TextureManager.createTexture(TextureKey("ice"), "/ice.png")
val RESPAWN_TEXTURE: Texture = TextureManager.createTexture(TextureKey("respawn"), "/respawn.png")
val SPIKE_TEXTURE: Texture = TextureManager.createTexture(TextureKey("spike"), "/spike.png")
val OSCILLATOR_TEXTURE: Texture = TextureManager.createTexture(TextureKey("oscillator"), "/oscillatorBlock.png")

//Polygons
