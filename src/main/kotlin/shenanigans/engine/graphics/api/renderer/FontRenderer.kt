package shenanigans.engine.graphics.api.renderer

import shenanigans.engine.graphics.api.font.BitmapFont

class FontRenderer : TextureRenderer() {

    override val fragShader: String
        get() =
            """
                #version 330

                in vec2 outTexCoord;
                in vec4 outColor;
                out vec4 fragColor;

                uniform sampler2D textureSampler;

                void main() {
                    vec4 sampleColor = vec4(outColor.rgb, texture(textureSampler, outTexCoord).r * outColor.a);
                    if(sampleColor.a == 0.0) {
                        discard;
                    }
                    fragColor = sampleColor;
                }
            """.trimIndent()

    fun drawText(font: BitmapFont, text: String, posX: Int, posY: Int, posZ: Float) {
        font.drawToFontRenderer(text, posX, posY, posZ, this)
    }
}