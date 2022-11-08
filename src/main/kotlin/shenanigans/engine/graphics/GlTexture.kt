package shenanigans.engine.graphics
import de.matthiasmann.twl.utils.PNGDecoder
import org.lwjgl.opengl.GL30C.*
import java.nio.ByteBuffer

class GlTexture(val width: Int, val height: Int, private val buf: ByteBuffer) {
    private val textureId = glGenTextures()

    init {
        glBindTexture(GL_TEXTURE_2D, textureId)
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
        glTexImage2D(GL_TEXTURE_2D,0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf)
        glBindTexture(GL_TEXTURE_2D, textureId)
        glPixelStorei(GL_UNPACK_ALIGNMENT,1)

        //filtering parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        //uploading texture data
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf)
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
