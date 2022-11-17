package shenanigans.engine.graphics
import de.matthiasmann.twl.utils.PNGDecoder
import org.lwjgl.opengl.GL30C.*
import java.nio.ByteBuffer

class GlTexture(val width: Int, val height: Int, private val buf: ByteBuffer, options: TextureOptions = TextureOptions()) {
    private val textureId = glGenTextures()

    init {
        glBindTexture(GL_TEXTURE_2D, textureId)
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
        glTexImage2D(GL_TEXTURE_2D, 0, options.textureType.glId, width, height, 0, options.textureType.glId, GL_UNSIGNED_BYTE, buf)

        // wrapping
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        // filtering parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, options.filterType.glId)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, options.filterType.glId)
    }

    fun discard() {
        glDeleteTextures(textureId)
    }

    fun bind() {
        //activate texture unit
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureId)
    }

    fun unbind() {
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    companion object {
        fun create(path: String): GlTexture {
            val decoder = PNGDecoder(GlTexture::class.java.getResourceAsStream(path))
            val buf = ByteBuffer.allocateDirect(4 * decoder.width * decoder.height)
            decoder.decode(buf, decoder.width * 4, PNGDecoder.Format.RGBA)
            buf.flip()

            return GlTexture(decoder.width, decoder.height, buf)
        }
    }

}
