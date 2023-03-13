package shenanigans.engine.graphics.api.font

import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBTTAlignedQuad
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTTPackContext
import org.lwjgl.stb.STBTTPackedchar
import org.lwjgl.stb.STBTruetype.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.stackPush
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.TextureOptions
import shenanigans.engine.graphics.api.renderer.FontRenderer
import shenanigans.engine.graphics.api.texture.Texture
import shenanigans.engine.graphics.api.texture.TextureManager
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.*

private typealias CodepointConsumer = (codepoint: Int, nextCodepoint: Int?) -> Unit

class BitmapFont internal constructor(
    val data: ByteBuffer,
    verticalMetrics: VerticalMetrics,
    private val info: STBTTFontinfo,
    val height: Float
) {
    private var bmpTexture: Texture

    private var context: STBTTPackContext
    private var characterData: STBTTPackedchar.Buffer
    val verticalMetrics: VerticalMetrics

    private val BITMAP_W = 512
    private val BITMAP_H = 512

    private val FIRST_CHAR = 32
    private val NUM_CHARS = 96

    private val measuredTextCache = WeakHashMap<String, Float>()

    private val quad: STBTTAlignedQuad = STBTTAlignedQuad.create()

    init {
        val scale = stbtt_ScaleForPixelHeight(info, height)
        this.verticalMetrics = VerticalMetrics(
            verticalMetrics.ascent * scale,
            verticalMetrics.descent * scale,
            verticalMetrics.lineGap * scale
        )

        val bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H)
        characterData = STBTTPackedchar.create(NUM_CHARS)

        // do some glfwGetMonitorContentScale sorta thing here

        context = STBTTPackContext.create()

        val success = stbtt_PackBegin(context, bitmap, BITMAP_W, BITMAP_H, 0, 1)
        if (!success) {
            throw RuntimeException("Failed to begin font packing")
        }

        val success2 = stbtt_PackFontRange(context, data, 0, height, FIRST_CHAR, characterData)
        if (!success2) {
            throw RuntimeException("Failed to pack font range")
        }

        stbtt_PackEnd(context)

        bmpTexture = TextureManager.createTextureFromData(
            TextureKey(),
            bitmap,
            BITMAP_W,
            BITMAP_H,
            TextureOptions(TextureOptions.TextureType.RED, TextureOptions.FilterType.LINEAR)
        )
    }

    private fun iterateCodepoints(text: String, stack: MemoryStack, func: CodepointConsumer) {
        val codepointBuf: IntBuffer = stack.mallocInt(1)
        var charIndex = 0
        val length: Int = text.length

        while (charIndex < length) {
            charIndex += getNextCodepoint(text, length, charIndex, codepointBuf)
            val codepoint: Int = codepointBuf.get(0)
            var next: Int? = null
            if (charIndex < length) {
                next = getNextCodepoint(text, length, charIndex, codepointBuf)
            }
            func(codepoint, next)
        }
    }

    fun drawToFontRenderer(text: String, posX: Int, posY: Int, renderer: FontRenderer) {
        val scale = stbtt_ScaleForPixelHeight(info, height)

        stackPush().use { stack ->
            val x: FloatBuffer = stack.floats(0.0f)
            val y: FloatBuffer = stack.floats(0.0f)
            quad.clear()
            iterateCodepoints(text, stack) { codepoint, next ->
                if (codepoint < FIRST_CHAR || FIRST_CHAR + NUM_CHARS <= codepoint) {
                    return@iterateCodepoints
                }
                stbtt_GetPackedQuad(characterData, BITMAP_W, BITMAP_H, codepoint - FIRST_CHAR, x, y, quad, true)

                if (next != null) {
                    x.put(0, x.get(0) + stbtt_GetCodepointKernAdvance(info, codepoint, next) * scale)
                }
                val width = quad.x1() - quad.x0()
                val height = quad.y1() - quad.y0()
                val texWidth = quad.s1() - quad.s0()
                val texHeight = quad.t1() - quad.t0()
                renderer.textureRect(
                    quad.x0() + posX, quad.y0() + posY, width, height, bmpTexture.getRegion(
                        quad.s0(), quad.t0(), texWidth, texHeight
                    )
                )
            }
        }
    }

    private fun getNextCodepoint(text: String, to: Int, i: Int, cpOut: IntBuffer): Int {
        val c1 = text[i]
        if (Character.isHighSurrogate(c1) && i + 1 < to) {
            val c2 = text[i + 1]
            if (Character.isLowSurrogate(c2)) {
                cpOut.put(0, Character.toCodePoint(c1, c2))
                return 2
            }
        }
        cpOut.put(0, c1.code)
        return 1
    }

    fun measureText(text: String): Float {
        if (measuredTextCache.containsKey(text)) {
            return measuredTextCache[text]!!
        }

        var width = 0f

        stackPush().use { stack ->
            val advance = stack.mallocInt(1)
            val bearing = stack.mallocInt(1)
            iterateCodepoints(text, stack) { codepoint, next ->
                if (codepoint < FIRST_CHAR || FIRST_CHAR + NUM_CHARS <= codepoint) {
                    return@iterateCodepoints
                }

                stbtt_GetCodepointHMetrics(info, codepoint, advance, bearing)
                width += advance.get(0)

                if (next != null) {
                    width += stbtt_GetCodepointKernAdvance(info, codepoint, next)
                }
            }
        }

        val result = width * stbtt_ScaleForPixelHeight(info, height)
        measuredTextCache[text] = result

        return result
    }

    fun discard() {

    }
}