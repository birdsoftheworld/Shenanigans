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
            uniform mat4 modelViewMatrix;

            void main() {
                gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
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

    private val lmesh = Mesh(
        floatArrayOf(
            0f, 100f, 0f,
            0f, 0f, 0f,
            100f, 0f, 0f,
            100f, 100f, 0f,
        ),
        intArrayOf(
            0, 1, 3, 3, 1, 2,
        ),
        floatArrayOf(
            0f, 1f,
            0f, 0f,
            1f, 0f,
            1f, 1f,
        )
    )

    private val mesh = Mesh(vertexCapacity, indicesCapacity, setOf(VertexAttribute.POSITION, VertexAttribute.TEX_COORDS))

    private var started = false

    private val indices = ArrayList<Int>(DEFAULT_MAX_INDICES)
    private val positions = ArrayList<Float>(DEFAULT_MAX_VERTICES*3)
    private val texCoords = ArrayList<Float>(DEFAULT_MAX_VERTICES*2)
    private val texture = Texture.create("/textureImage.png")

    private var lowestIndex = 0

    init {
        shader.createUniform("textureSampler")
        shader.createUniform("projectionMatrix")
        shader.createUniform("modelViewMatrix")
    }

    constructor() : this(null, DEFAULT_MAX_VERTICES, DEFAULT_MAX_INDICES)

    fun textureRect(x: Float, y: Float, w: Float, h: Float, texture: Texture){
        addIndex(0)
        addIndex(2)
        addIndex(1)

        addIndex(2)
        addIndex(3)
        addIndex(1)

        addVertex(x, y)
        addVertex(x + w, y)
        addVertex(x, y + h)
        addVertex(x + w, y + h)
    }

    private fun addIndex(index: Int) {
        indices.add(index + lowestIndex)
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

        mesh.writeIndices(indices.toIntArray())
        mesh.writeData(VertexAttribute.POSITION, positions.toFloatArray())
        mesh.writeData(VertexAttribute.TEX_COORDS, texCoords.toFloatArray())

        this.render()

        indices.clear()
        positions.clear()
        texture.discard()
        lowestIndex = 0
        started = false
    }

    private fun render() {
        shader.bind()
        shader.setUniform("projectionMatrix", camera!!.getProjectionMatrix())
        texture.bind()
        mesh.render()
        texture.unbind()
        shader.unbind()
    }
}