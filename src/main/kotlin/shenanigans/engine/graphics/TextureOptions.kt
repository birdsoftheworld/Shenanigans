package shenanigans.engine.graphics

import org.lwjgl.opengl.GL30C.*

data class TextureOptions(
    val textureType: TextureType = TextureType.RGBA,
    val filterType: FilterType = FilterType.NEAREST_NEIGHBOR,
    val wrapping: WrappingType = WrappingType.CLAMP_TO_EDGE
) {
    enum class TextureType(val glId: Int) {
        RGBA(GL_RGBA),
        RED(GL_RED)
    }

    enum class FilterType(val glId: Int) {
        NEAREST_NEIGHBOR(GL_NEAREST),
        LINEAR(GL_LINEAR)
    }

    enum class WrappingType(val glId: Int) {
        CLAMP_TO_EDGE(GL_CLAMP_TO_EDGE),
        REPEAT(GL_REPEAT)
    }
}