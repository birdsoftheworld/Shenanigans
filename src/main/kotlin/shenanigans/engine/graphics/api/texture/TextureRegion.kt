package shenanigans.engine.graphics.api.texture

import shenanigans.engine.graphics.GlTexture
import shenanigans.engine.graphics.TextureKey

class TextureRegion internal constructor(private val texture: Texture, val x: Float, val y: Float, val w: Float, val h: Float) {
    fun getGlTexture() : GlTexture = texture.getGlTexture()
    fun getKey() : TextureKey = texture.key
}