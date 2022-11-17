package shenanigans.engine.graphics.api.texture

import org.joml.Vector2i
import shenanigans.engine.graphics.GlTexture
import shenanigans.engine.graphics.TextureOptions
import shenanigans.engine.graphics.GlobalRendererState
import shenanigans.engine.graphics.TextureKey
import java.nio.ByteBuffer

object TextureManager {
    private val queuedTextures = mutableListOf<Pair<TextureKey, String>>()
    private val queuedRawTextures = mutableListOf<Pair<TextureKey, Triple<ByteBuffer, Vector2i, TextureOptions>>>()
    private val keyedTextures = mutableMapOf<TextureKey, GlTexture>()

    fun createTexture(path: String) : Texture {
        val key = TextureKey()
        queuedTextures.add(Pair(key, path))
        if(GlobalRendererState.isInitializedAndOnRenderThread()) {
            dequeue()
        }
        return Texture(key)
    }

    fun createTextureFromData(data: ByteBuffer, width: Int, height: Int, options: TextureOptions = TextureOptions()) : Texture {
        val key = TextureKey()
        queuedRawTextures.add(Pair(key, Triple(data, Vector2i(width, height), options)))
        if(GlobalRendererState.isInitializedAndOnRenderThread()) {
            dequeue()
        }
        return Texture(key)
    }

    internal fun dequeue() {
        for (queuedTexture in queuedTextures) {
            createTexture(queuedTexture.second, queuedTexture.first)
        }
        for (queuedRawTexture in queuedRawTextures) {
            createRawTexture(
                queuedRawTexture.second.first,
                queuedRawTexture.second.second.x,
                queuedRawTexture.second.second.y,
                queuedRawTexture.second.third,
                queuedRawTexture.first
            )
        }
        queuedTextures.clear()
    }

    internal fun initialize() {
        dequeue()
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

    private fun createRawTexture(data: ByteBuffer, width: Int, height: Int, options: TextureOptions, key: TextureKey) {
        val glTexture = GlTexture(width, height, data, options)
        keyedTextures[key] = glTexture
    }

    fun discard() {
        for (texture in keyedTextures.values) {
            texture.discard()
        }
        keyedTextures.clear()
    }
}