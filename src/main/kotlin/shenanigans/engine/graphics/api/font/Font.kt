package shenanigans.engine.graphics.api.font

import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBTTAlignedQuad
import org.lwjgl.stb.STBTTBakedChar
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype.*
import org.lwjgl.system.MemoryStack.stackPush
import shenanigans.engine.graphics.GlTexture
import shenanigans.engine.graphics.api.TextureRenderer
import shenanigans.engine.graphics.api.texture.Texture
import shenanigans.engine.graphics.api.texture.TextureManager
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Font(val data: ByteBuffer, val height: Float) {
    private val info = STBTTFontinfo.create()
    private var bmpTexture: Texture

    private var verticalMetrics: VerticalMetrics
    private var characterData: STBTTBakedChar.Buffer

    private val BITMAP_W = 512
    private val BITMAP_H = 512

    init {
        if(!stbtt_InitFont(info, data)) {
            throw RuntimeException("Failed to load font")
        }

        characterData = STBTTBakedChar.malloc(96)
        val bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H)

        stackPush().use {
            val ascent = it.mallocInt(1)
            val descent = it.mallocInt(1)
            val lineGap = it.mallocInt(1)

            stbtt_GetFontVMetrics(info, ascent, descent, lineGap)
            verticalMetrics = VerticalMetrics(ascent.get(0), ascent.get(0), ascent.get(0))
        }

        // do some glfwGetMonitorContentScale sorta thing here

        stbtt_BakeFontBitmap(data, height, bitmap, BITMAP_W, BITMAP_H, 32, characterData)

        bmpTexture = TextureManager.createTextureFromData(bitmap, BITMAP_W, BITMAP_H, GlTexture.TextureType.A)
    }

    fun drawToTextureRenderer(text: String, posX: Int, posY: Int, renderer: TextureRenderer) {
        val scale = stbtt_ScaleForPixelHeight(info, height)

        stackPush().use { stack ->
            val codepointBuf: IntBuffer = stack.mallocInt(1)
            val x: FloatBuffer = stack.floats(0.0f)
            val y: FloatBuffer = stack.floats(0.0f)
            val quad: STBTTAlignedQuad = STBTTAlignedQuad.malloc(stack)
            var lineStart = 0
            val factorX: Float = 1.0f / 1 // content scale x
            val factorY: Float = 1.0f / 1 // content scale y
            var lineY = 0.0f
            var charIndex = 0
            val length: Int = text.length
            while (charIndex < length) {
                charIndex += getNextCodepoint(text, length, charIndex, codepointBuf)
                val codepoint: Int = codepointBuf.get(0)
                /*if (cp == '\n'.code) {
                    y.put(0, y.get(0) + (ascent - descent + lineGap) * scale.also { lineY = it })
                    x.put(0, 0.0f)
                    lineStart = i
                    continue
                } else*/ if (codepoint < 32 || 128 <= codepoint) {
                    continue
                }
//                val xBefore: Float = x.get(0)
                stbtt_GetBakedQuad(characterData, BITMAP_W, BITMAP_H, codepoint - 32, x, y, quad, true)
//                x.put(0, scale(xBefore, x.get(0), factorX))

                if (/* kerning */ true && charIndex < length) {
                    getNextCodepoint(text, length, charIndex, codepointBuf)
                    x.put(0, x.get(0) + stbtt_GetCodepointKernAdvance(info, codepoint, codepointBuf.get(0)) * scale)
                }
//                val x0: Float = scale(xBefore, q.x0(), factorX)
//                val x1: Float = scale(xBefore, q.x1(), factorX)
//                val y0: Float = scale(lineY, q.y0(), factorY)
//                val y1: Float = scale(lineY, q.y1(), factorY)
                val width = quad.x1() - quad.x0()
                val height = quad.y1() - quad.y0()
                renderer.textureRect(quad.x0() + posX, quad.y0() + posY, width, height, bmpTexture.getRegion(
                    quad.x0(), quad.y0(), width, height
                ))
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

    companion object {
        fun fromFile(path: String, height: Float) : Font {
            val inputStream = Font::class.java.getResourceAsStream(path)!!
            val bytes = inputStream.readBytes()
            val buf = BufferUtils.createByteBuffer(bytes.size)
            buf.put(bytes).flip()
            return Font(buf, height)
        }
    }
}