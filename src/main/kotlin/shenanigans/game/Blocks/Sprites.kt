package shenanigans.game.Blocks

import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.ComponentView
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.util.shapes.Rectangle

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

    val nullSprite = Sprite(TextureManager.createTexture("/nullSprite.png").getRegion(), Rectangle(30f,30f))
    val playerSprite = Sprite(TextureManager.createTexture("/playerTexture.png").getRegion(), Rectangle(30f,30f))
    val respawnSprite = Sprite(TextureManager.createTexture("/sprite.png").getRegion(), Rectangle(30f,30f))
    val oscillatingSprite = Sprite(TextureManager.createTexture("/betterArrow.png").getRegion(), Rectangle(50f,50f))
    val trampolineSprite = Sprite(TextureManager.createTexture("/spring.png").getRegion(), Rectangle(50f,50f))
    val spikeSprite = Sprite(TextureManager.createTexture("/hole.png").getRegion(), Rectangle(50f,50f))
    val teleportarASprite = Sprite(TextureManager.createTexture("/teleporterA.png").getRegion(), Rectangle(25f,25f))
    val teleportarBSprite = Sprite(TextureManager.createTexture("/teleporterB.png").getRegion(), Rectangle(25f,25f))

}