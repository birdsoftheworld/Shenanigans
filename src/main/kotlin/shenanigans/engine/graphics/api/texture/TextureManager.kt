package shenanigans.engine.graphics.api.texture

import shenanigans.engine.graphics.GlTexture
import shenanigans.engine.graphics.TextureKey

object TextureManager {
    private val queuedTextures = mutableListOf<Pair<TextureKey, String>>()
    private val keyedTextures = mutableMapOf<TextureKey, GlTexture>()

    fun createTexture(path: String): Texture {
        val key = TextureKey()
        queuedTextures.add(Pair(key, path))
        return Texture(key)
    }

    internal fun initialize() {
        for (queuedTexture in queuedTextures) {
            createTexture(queuedTexture.second, queuedTexture.first)
        }
        queuedTextures.clear()
    }

    internal fun getTexture(key: TextureKey) : GlTexture {
        return keyedTextures[key]!!
    }

    private fun createTexture(path: String, key: TextureKey) {
        val glTexture = GlTexture.create(
            path
        )
        keyedTextures[key] = glTexture
    }

    fun discard() {
        for (texture in keyedTextures.values) {
            texture.discard()
        }
        keyedTextures.clear()
    }
}