package shenanigans.engine.graphics
import de.matthiasmann.twl.utils.PNGDecoder
import org.lwjgl.opengl.GL11.*
import java.nio.ByteBuffer

class Texture {
    private val decoder : PNGDecoder = PNGDecoder(Texture::class.java.getResourceAsStream("/textureImage.png"))  //input texture image
    private val buf = ByteBuffer.allocateDirect(4*decoder.width * decoder.height)
    val textureId = glGenTextures()


    init{
        decoder.decode(buf, decoder.width * 4, PNGDecoder.Format.RGBA)
        buf.flip()
        glBindTexture(GL_TEXTURE_2D, textureId)
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.width,
            decoder.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf)
        glBindTexture(GL_TEXTURE_2D, textureId)
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)

        //filtering parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        //uploading texture data
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.width, decoder.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf)
    }
}
