package shenanigans.engine.graphics.api

class FontRenderer : TextureRenderer() {
    override val fragShader: String
        get() =
            """
                #version 330

                in vec2 outTexCoord;
                out vec4 fragColor;

                uniform sampler2D textureSampler;

                void main() {
                    vec4 sampleColor = vec4(1.0, 1.0, 1.0, texture(textureSampler, outTexCoord).r);
                    fragColor = sampleColor;
                }
            """.trimIndent()
}