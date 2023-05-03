package shenanigans.engine.graphics.api.renderer

import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.VertexAttribute
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.graphics.api.texture.TextureRegion
import shenanigans.engine.graphics.shader.Shader

open class TextureRenderer(vertexCapacity: Int = DEFAULT_MAX_VERTICES, indicesCapacity: Int = DEFAULT_MAX_INDICES) :
    AbstractRenderer(
        setOf(
            VertexAttribute.POSITION, VertexAttribute.TEX_COORDS, VertexAttribute.COLOR, VertexAttribute.ALPHA
        ), vertexCapacity, indicesCapacity
    ) {
    protected open val vertexShader: String =
        """
            #version 330

            layout (location=0) in vec3 position;
            layout (location=1) in vec2 texCoord;
            layout (location=2) in vec3 color;
            layout (location=3) in float alpha;

            out vec2 outTexCoord;
            out vec4 outColor;

            uniform mat4 projectionMatrix;

            void main() {
                gl_Position = projectionMatrix * vec4(position, 1.0);
                outTexCoord = texCoord;
                outColor = vec4(color, alpha);
            }
        """.trimIndent()

    protected open val fragShader: String =
        """
            #version 330

            in vec2 outTexCoord;
            in vec4 outColor;
            out vec4 fragColor;

            uniform sampler2D textureSampler;

            void main() {
                vec4 sample = texture(textureSampler, outTexCoord);
                fragColor = vec4(sample.rgb * outColor.rgb, sample.a * outColor.a);
            }
        """.trimIndent()

    final override val shader = Shader(
        vertexShader, fragShader
    )

    private val texCoords = ArrayList<Float>(DEFAULT_MAX_VERTICES * 2)
    private val colors = ArrayList<Float>(DEFAULT_MAX_VERTICES * 3)
    private val alphas = ArrayList<Float>(DEFAULT_MAX_VERTICES)
    private var texture: TextureKey? = null
    var tint: Color = Color(1f, 1f, 1f)

    init {
        shader.createUniform("textureSampler")
        shader.createUniform("projectionMatrix")
    }

    fun textureRect(x: Float, y: Float, w: Float, h: Float, texture: TextureRegion) =
        textureRect(x, y, 0f, w, h, texture)

    fun textureRect(x: Float, y: Float, z: Float, w: Float, h: Float, texture: TextureRegion) {
        if (this.texture != texture.getKey() && this.texture != null) {
            this.flush()
        }
        flushIfFull(4, 6)

        this.texture = texture.getKey()
        addIndex(0)
        addIndex(2)
        addIndex(1)

        addIndex(2)
        addIndex(3)
        addIndex(1)

        addVertex(x, y, z)
        addTexCoord(texture.x, texture.y)
        addVertex(x + w, y, z)
        addTexCoord(texture.x + texture.w, texture.y)
        addVertex(x, y + h, z)
        addTexCoord(texture.x, texture.y + texture.h)
        addVertex(x + w, y + h, z)
        addTexCoord(texture.x + texture.w, texture.y + texture.h)

        for (i in 0..3) {
            addColor(tint)
        }
    }

    private fun addColor(color: Color) {
        colors.add(color.r)
        colors.add(color.g)
        colors.add(color.b)
        alphas.add(color.a)
    }

    private fun addTexCoord(x: Float, y: Float) {
        texCoords.add(x)
        texCoords.add(y)
    }

    override fun writeVertexAttributes() {
        mesh.writeData(VertexAttribute.TEX_COORDS, texCoords.toFloatArray())
        mesh.writeData(VertexAttribute.COLOR, colors.toFloatArray())
        mesh.writeData(VertexAttribute.ALPHA, alphas.toFloatArray())
    }

    override fun clearVertexAttributes() {
        texCoords.clear()
        colors.clear()
        alphas.clear()
        tint = Color(1f, 1f, 1f)
    }

    override fun setUniforms() {
        shader.setUniform("projectionMatrix", projection)
    }

    override fun createUniforms() {
        shader.createUniform("projectionMatrix")
    }

    override fun render() {
        if (texture == null) return
        val texture = TextureManager.getTexture(texture!!)
        texture.bind()
        super.render()
        texture.unbind()
    }
}