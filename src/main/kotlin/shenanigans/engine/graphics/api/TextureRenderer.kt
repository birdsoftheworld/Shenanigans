package shenanigans.engine.graphics.api

import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.VertexAttribute
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.graphics.api.texture.TextureRegion
import shenanigans.engine.graphics.shader.Shader

open class TextureRenderer(vertexCapacity: Int = DEFAULT_MAX_VERTICES, indicesCapacity: Int = DEFAULT_MAX_INDICES) : AbstractRenderer(setOf(
    VertexAttribute.POSITION, VertexAttribute.TEX_COORDS
), vertexCapacity, indicesCapacity) {

    protected open val vertexShader: String =
        """
            #version 330

            layout (location=0) in vec3 position;
            layout (location=1) in vec2 texCoord;

            out vec2 outTexCoord;

            uniform mat4 projectionMatrix;

            void main() {
                gl_Position = projectionMatrix * vec4(position, 1.0);
                outTexCoord = texCoord;
            }
        """.trimIndent()

    protected open val fragShader: String =
        """
            #version 330

            in vec2 outTexCoord;
            out vec4 fragColor;

            uniform sampler2D textureSampler;

            void main() {
                fragColor = texture(textureSampler, outTexCoord);
            }
        """.trimIndent()

    override val shader = Shader(
        vertexShader, fragShader
    )

    private val texCoords = ArrayList<Float>(DEFAULT_MAX_VERTICES*2)
    private var texture: TextureKey? = null

    init {
        shader.createUniform("textureSampler")
        shader.createUniform("projectionMatrix")
    }

    private fun renderCurrent() {
        if(lowestIndex != 0) {
            this.writeToMesh()
            this.render()
        }

        this.clear()
    }

    fun textureRect(x: Float, y: Float, w: Float, h: Float, texture: TextureRegion) {
        if(this.texture != texture.getKey() && this.texture != null) {
            this.renderCurrent()
        }
        this.texture = texture.getKey()
        addIndex(0)
        addIndex(2)
        addIndex(1)

        addIndex(2)
        addIndex(3)
        addIndex(1)

        addVertex(x, y)
        addTexCoord(texture.x, texture.y)
        addVertex(x + w, y)
        addTexCoord(texture.x + texture.w, texture.y)
        addVertex(x, y + h)
        addTexCoord(texture.x, texture.y + texture.h)
        addVertex(x + w, y + h)
        addTexCoord(texture.x + texture.w, texture.y + texture.h)
    }

    private fun addTexCoord(x: Float, y: Float) {
        texCoords.add(x)
        texCoords.add(y)
    }

    override fun writeVertexAttributes() {
        mesh.writeData(VertexAttribute.TEX_COORDS, texCoords.toFloatArray())
    }

    override fun clearVertexAttributes() {
        texCoords.clear()
    }

    override fun setUniforms() {
        shader.setUniform("projectionMatrix", projection)
    }

    override fun createUniforms() {
        shader.createUniform("projectionMatrix")
    }

    override fun render() {
        if(texture == null) return
        val texture = TextureManager.getTexture(texture!!)
        texture.bind()
        super.render()
        texture.unbind()
    }
}