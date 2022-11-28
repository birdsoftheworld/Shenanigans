package shenanigans.engine.graphics.api.font

import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.stb.STBTruetype.*
import java.nio.ByteBuffer

class Font(val data: ByteBuffer) {
    private val info = STBTTFontinfo.create()

    private var verticalMetrics: VerticalMetrics

    init {
        if (!stbtt_InitFont(info, data)) {
            throw RuntimeException("Failed to load font")
        }

        stackPush().use {
            val ascent = it.mallocInt(1)
            val descent = it.mallocInt(1)
            val lineGap = it.mallocInt(1)

            stbtt_GetFontVMetrics(info, ascent, descent, lineGap)
            verticalMetrics = VerticalMetrics(ascent.get(0), ascent.get(0), ascent.get(0))
        }
    }

    fun createSized(height: Float): BitmapFont {
        return BitmapFont(data, verticalMetrics, info, height)
    }

    companion object {
        fun fromFile(path: String): Font {
            val inputStream = BitmapFont::class.java.getResourceAsStream(path)!!
            val bytes = inputStream.readBytes()
            val buf = BufferUtils.createByteBuffer(bytes.size)
            buf.put(bytes).flip()
            return Font(buf)
        }
    }
}