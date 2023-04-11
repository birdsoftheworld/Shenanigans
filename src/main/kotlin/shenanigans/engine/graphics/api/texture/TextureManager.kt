package shenanigans.engine.graphics.api.texture

import org.joml.Vector2i
import shenanigans.engine.graphics.GlTexture
import shenanigans.engine.graphics.GlobalRendererState
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.TextureOptions
import java.nio.ByteBuffer

object TextureManager {
    private val queuedTextures = mutableListOf<Pair<TextureKey, TextureCreatable>>()
    private val keyedTextures = mutableMapOf<TextureKey, GlTexture>()

    fun createTexture(key: TextureKey, path: String, options: TextureOptions = TextureOptions()) : Texture {
        queuedTextures.add(Pair(key, PathTexture(options, path)))
        if(GlobalRendererState.isInitializedAndOnRenderThread()) {
            dequeue()
        }
        return Texture(key)
    }

    fun createTextureFromData(key: TextureKey, data: ByteBuffer, width: Int, height: Int, options: TextureOptions = TextureOptions()) : Texture {
        queuedTextures.add(Pair(key, RawTexture(options, data, Vector2i(width, height))))
        if(GlobalRendererState.isInitializedAndOnRenderThread()) {
            dequeue()
        }
        return Texture(key)
    }

    internal fun dequeue() {
        for (queuedTexture in queuedTextures) {
            keyedTextures[queuedTexture.first] = queuedTexture.second.create()
        }
        queuedTextures.clear()
    }

    internal fun initialize() {
        dequeue()
    }

    internal fun getTexture(key: TextureKey) : GlTexture {
        return keyedTextures[key]!!
    }

    fun discard() {
        for (texture in keyedTextures.values) {
            texture.discard()
        }
        keyedTextures.clear()
    }
}

private abstract class TextureCreatable(val options: TextureOptions) {
    abstract fun create() : GlTexture
}

private class PathTexture(options: TextureOptions, val path: String) : TextureCreatable(options) {
    override fun create(): GlTexture {
        return GlTexture.create(path, options)
    }
}

private class RawTexture(options: TextureOptions, val data: ByteBuffer, val size: Vector2i) : TextureCreatable(options) {
    override fun create(): GlTexture {
        return GlTexture(size.x, size.y, data, options)
    }
}