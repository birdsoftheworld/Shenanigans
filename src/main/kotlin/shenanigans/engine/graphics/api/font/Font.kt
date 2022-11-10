package shenanigans.engine.graphics.api.font

import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype.*
import shenanigans.engine.graphics.api.TextureRenderer
import shenanigans.engine.graphics.api.texture.Texture
import shenanigans.engine.graphics.api.texture.TextureManager
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer

class Font(data: ByteBuffer) {
    private val info = STBTTFontinfo.create()
    private var bmpTexture: Texture? = null

    private var verticalMetrics: VerticalMetrics? = null

    init {
        if(!stbtt_InitFont(info, data)) {
            throw RuntimeException("Failed to load font")
        }



        val scale = stbtt_ScaleForPixelHeight(info, 30f)

        bmpTexture = TextureManager.createTextureFromData()
    }

    fun drawToTextureRenderer(text: String, renderer: TextureRenderer) {

    }

    companion object {
        fun fromFile(path: String) : Font {
            return fromFile(File(path))
        }

        fun fromFile(file : File) : Font {
            val inputStream = FileInputStream(file)
            val bytes = inputStream.readBytes()
            val buf = BufferUtils.createByteBuffer(bytes.size)
            buf.put(bytes).flip()
            return Font(buf)
        }
    }
}