package shenanigans.game.blocks

import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.util.shapes.Rectangle
import shenanigans.game.player.PlayerController

object Sprites {
    fun getSprite(block : Component): Sprite {
        when(block) {
            is SpikeBlock-> return spikeSprite
            is TrampolineBlock -> return trampolineSprite
            is OscillatingBlock -> return oscillatingSprite
            is TeleporterBlock -> return nullSprite
        }
        return nullSprite
    }

    val nullSpriteTexture = TextureManager.createTexture(TextureKey("null"),"/null.png")
    val playerSpriteTexture = TextureManager.createTexture(TextureKey("player"),"/playerTexture.png")
    val oscillatingSpriteTexture = TextureManager.createTexture(TextureKey("arrow"),"/betterArrow.png")
    val springSpriteTexture = TextureManager.createTexture(TextureKey("spring"),"/spring.png")
    val spikeSpriteTexture = TextureManager.createTexture(TextureKey("hole"),"/hole.png")
    val teleporterASpriteTexture = TextureManager.createTexture(TextureKey("teleportA"),"/teleporterA.png")
    val teleporterBSpriteTexture = TextureManager.createTexture(TextureKey("teleportB"),"/teleporterB.png")

    val nullSprite
        get() = Sprite(nullSpriteTexture.getRegion(), Rectangle(50f,50f))
    val playerSprite
        get() = Sprite(playerSpriteTexture.getRegion(), PlayerController.SHAPE_BASE)
    val oscillatingSprite
        get() = Sprite(oscillatingSpriteTexture.getRegion(), Rectangle(50f,50f))
    val trampolineSprite
        get() = Sprite(springSpriteTexture.getRegion(), Rectangle(50f,50f))
    val spikeSprite
        get() = Sprite(spikeSpriteTexture.getRegion(), Rectangle(50f,50f))
    val teleporterASprite
        get() = Sprite(teleporterASpriteTexture.getRegion(), Rectangle(25f,25f))
    val teleporterBSprite
        get() = Sprite(teleporterBSpriteTexture.getRegion(), Rectangle(25f,25f))
}