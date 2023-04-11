package shenanigans.game.Blocks

import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.ComponentView
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.util.shapes.Rectangle
import shenanigans.game.player.Player

class Sprites {
    fun getSprite(block : Component): Sprite {
        when(block){
            is SpikeBlock-> return spikeSprite
            is TrampolineBlock -> return trampolineSprite
            is OscillatingBlock -> return oscillatingSprite
            is TeleporterBlock -> return nullSprite
        }
        return nullSprite
    }
    val nullSprite = Sprite(TextureManager.createTexture(TextureKey("null"),"/nullSprite.png").getRegion(), Rectangle(50f,50f))
    val playerSprite = Sprite(TextureManager.createTexture(TextureKey("player"),"/playerTexture.png").getRegion(), Rectangle(25f,25f))
    val oscillatingSprite = Sprite(TextureManager.createTexture(TextureKey("arrow"),"/betterArrow.png").getRegion(), Rectangle(50f,50f))
    val trampolineSprite = Sprite(TextureManager.createTexture(TextureKey("spring"),"/spring.png").getRegion(), Rectangle(50f,50f))
    val spikeSprite = Sprite(TextureManager.createTexture(TextureKey("hole"),"/hole.png").getRegion(), Rectangle(50f,50f))
    val teleportarASprite = Sprite(TextureManager.createTexture(TextureKey("teleportA"),"/teleporterA.png").getRegion(), Rectangle(25f,25f))
    val teleportarBSprite = Sprite(TextureManager.createTexture(TextureKey("teleportB"),"/teleporterB.png").getRegion(), Rectangle(25f,25f))

}