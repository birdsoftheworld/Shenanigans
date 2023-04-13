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

    val nullSprite = Sprite(TextureManager.createTexture(TextureKey("null"),"/null.png").getRegion(), Rectangle(50f,50f))
    val playerSprite = Sprite(TextureManager.createTexture(TextureKey("player"),"/playerTexture.png").getRegion(), PlayerController.SHAPE_BASE)
    val oscillatingSprite = Sprite(TextureManager.createTexture(TextureKey("arrow"),"/betterArrow.png").getRegion(), Rectangle(50f,50f))
    val trampolineSprite = Sprite(TextureManager.createTexture(TextureKey("spring"),"/spring.png").getRegion(), Rectangle(50f,50f))
    val spikeSprite = Sprite(TextureManager.createTexture(TextureKey("hole"),"/hole.png").getRegion(), Rectangle(50f,50f))
    val teleporterASprite = Sprite(TextureManager.createTexture(TextureKey("teleportA"),"/teleporterA.png").getRegion(), Rectangle(25f,25f))
    val teleporterBSprite = Sprite(TextureManager.createTexture(TextureKey("teleportB"),"/teleporterB.png").getRegion(), Rectangle(25f,25f))
}