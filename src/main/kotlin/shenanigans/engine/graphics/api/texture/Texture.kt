package shenanigans.engine.graphics.api.texture

import shenanigans.engine.graphics.GlTexture
import shenanigans.engine.graphics.TextureKey

class Texture internal constructor(val key: TextureKey) {
    fun getRegion(x: Float, y: Float, w: Float, h: Float): TextureRegion = TextureRegion(this, x, y, w, h)
    fun getRegion(): TextureRegion = TextureRegion(this, 0f, 0f, 1f, 1f)

    fun getGlTexture(): GlTexture {
        return TextureManager.getTexture(key)
    }
}