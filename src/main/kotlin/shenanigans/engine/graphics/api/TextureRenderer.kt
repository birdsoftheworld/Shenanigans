package shenanigans.engine.graphics.api

import shenanigans.engine.graphics.Mesh
import shenanigans.engine.graphics.Texture
import shenanigans.engine.graphics.VertexAttribute
import shenanigans.engine.graphics.shader.Shader
import shenanigans.engine.util.OrthoCamera
import java.lang.IllegalStateException


private const val DEFAULT_MAX_VERTICES = 500
private const val DEFAULT_MAX_INDICES = 250

class TextureRenderer(var camera: OrthoCamera?, vertexCapacity: Int, indicesCapacity: Int) {

    private val shader = Shader(
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
        """.trimIndent(),
        """
            #version 330

            in vec2 outTexCoord;
            out vec4 fragColor;

            uniform sampler2D textureSampler;

            void main() {
                fragColor = texture(textureSampler, outTexCoord);
            }
        """.trimIndent(),
    )

    private val mesh = Mesh(vertexCapacity, indicesCapacity, setOf(VertexAttribute.POSITION, VertexAttribute.TEX_COORDS))

    private var started = false

    private val indices = ArrayList<Int>(DEFAULT_MAX_INDICES)
    private val positions = ArrayList<Float>(DEFAULT_MAX_VERTICES*3)
    private val texCoords = ArrayList<Float>(DEFAULT_MAX_VERTICES*2)
    private var texture: Texture? = null

    private var lowestIndex = 0

    init {
        shader.createUniform("textureSampler")
        shader.createUniform("projectionMatrix")
    }

    constructor() : this(null, DEFAULT_MAX_VERTICES, DEFAULT_MAX_INDICES)


    private fun renderCurrent(){
        mesh.writeIndices(indices.toIntArray())
        mesh.writeData(VertexAttribute.POSITION, positions.toFloatArray())
        mesh.writeData(VertexAttribute.TEX_COORDS, texCoords.toFloatArray())

        this.render()

        indices.clear()
        positions.clear()
        texCoords.clear()
        lowestIndex = 0
    }
    fun textureRect(x: Float, y: Float, w: Float, h: Float, texture: Texture){
        if(this.texture != texture && this.texture != null){
            this.renderCurrent()
        }
        this.texture = texture
        addIndex(0)
        addIndex(2)
        addIndex(1)

        addIndex(2)
        addIndex(3)
        addIndex(1)

        addVertex(x, y)
        addTexCoord(0f, 0f)
        addVertex(x + w, y)
        addTexCoord(1f, 0f)
        addVertex(x, y + h)
        addTexCoord(0f, 1f)
        addVertex(x + w, y + h)
        addTexCoord(1f, 1f)
    }

    private fun addIndex(index: Int) {
        indices.add(index + lowestIndex)
    }

    private fun addTexCoord(x: Float, y: Float) {
        texCoords.add(x)
        texCoords.add(y)
    }

    private fun addVertex(x: Float, y: Float) {
        positions.add(x)
        positions.add(y)
        positions.add(0f)
        lowestIndex++
    }

    fun start() {
        if(started) throw IllegalStateException("Must end rendering before starting")
        started = true
    }

    fun end() {
        if(!started) throw IllegalStateException("Must start rendering before ending")

        renderCurrent()

        started = false
        this.texture = null
    }

    private fun render() {
        shader.bind()
        shader.setUniform("projectionMatrix", camera!!.getProjectionMatrix())
        texture!!.bind()
        mesh.render()
        texture!!.unbind()
        shader.unbind()
    }
}