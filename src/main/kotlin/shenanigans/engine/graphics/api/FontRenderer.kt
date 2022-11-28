package shenanigans.engine.graphics.api

import shenanigans.engine.graphics.api.font.BitmapFont

class FontRenderer : TextureRenderer() {

    override val fragShader: String
        get() =
            """
                #version 330

                in vec2 outTexCoord;
                in vec3 outColor;
                out vec4 fragColor;

                uniform sampler2D textureSampler;

                void main() {
                    vec4 sampleColor = vec4(outColor.rgb, texture(textureSampler, outTexCoord).r);
                    fragColor = sampleColor;
                }
            """.trimIndent()

    fun drawText(font: BitmapFont, text: String, posX: Int, posY: Int) {
        font.drawToFontRenderer(text, posX, posY, this)
    }
}