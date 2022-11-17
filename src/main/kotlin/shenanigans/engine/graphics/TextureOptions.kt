package shenanigans.engine.graphics

import org.lwjgl.opengl.GL30C.*

data class TextureOptions(
    val textureType: TextureType = TextureType.RGBA,
    val filterType: FilterType = FilterType.NEAREST_NEIGHBOR
) {
    enum class TextureType(val glId: Int) {
        RGBA(GL_RGBA),
        RED(GL_RED)
    }

    enum class FilterType(val glId: Int) {
        NEAREST_NEIGHBOR(GL_NEAREST),
        LINEAR(GL_LINEAR)
    }
}