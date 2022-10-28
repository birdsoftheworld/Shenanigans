package shenanigans.engine.graphics.api

import org.joml.Vector2f
import shenanigans.engine.graphics.*
import shenanigans.engine.graphics.shader.Shader
import shenanigans.engine.util.OrthoCamera
import java.lang.IllegalStateException

private const val DEFAULT_MAX_VERTICES = 500
private const val DEFAULT_MAX_INDICES = 250

class ShapeRenderer(var camera: OrthoCamera?, vertexCapacity: Int, indicesCapacity: Int) {
//    private val shader = Shader(
//        """
//            #version 330
//
//            layout (location=0) in vec3 position;
//
//            uniform mat4 projectionMatrix;
//            uniform mat4 modelViewMatrix;
//
//            void main() {
//                gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
//            }
//        """.trimIndent(),
//        """
//            #version 330
//
//            out vec4 fragColor;
//
//            void main() {
//                fragColor = vec4(1.0, 0.0, 1.0, 1.0);
//            }
//        """.trimIndent(),
//    )
private val shader = Shader(
    """
            #version 330

            layout (location=0) in vec3 position;
            layout (location=1) in vec3 inColor;

            out vec3 outColor;
            
            uniform mat4 projectionMatrix;
            uniform mat4 modelViewMatrix;

            void main() {
                gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
                outColor = inColor;
            }
        """.trimIndent(),
    """
            #version 330

            in vec3 outColor;
            out vec4 fragColor;

            void main() {
                fragColor = vec4(outColor, 1.0);
            }
        """.trimIndent(),
)

//    private val mesh = Mesh(vertexCapacity, indicesCapacity, setOf(VertexAttribute.POSITION))
private val mesh = Mesh(
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
        0.5f, 0.0f, 0.0f,
        0.0f, 0.5f, 0.0f,
        0.0f, 0.0f, 0.5f,
        0.0f, 0.5f, 0.5f,
    ),
)

    private var started = false

    private val indices = ArrayList<Int>(DEFAULT_MAX_INDICES)
    private val positions = ArrayList<Float>(DEFAULT_MAX_VERTICES * 3)
    private val colors = ArrayList<Float>(DEFAULT_MAX_VERTICES * 3)

    init {
        shader.createUniform("projectionMatrix")
        shader.createUniform("modelViewMatrix")
    }

    constructor() : this(null, DEFAULT_MAX_VERTICES, DEFAULT_MAX_INDICES)

    fun rect(x: Float, y: Float, w: Float, h: Float, color: Color) {
//        addVertex(x, y)
//        addVertex(x + w, y)
//        addVertex(x, y + h)
//        addVertex(x + w, y + h)
//
//        addIndex(0)
//        addIndex(2)
//        addIndex(1)
//
//        addIndex(2)
//        addIndex(3)
//        addIndex(1)

        addVertex(x, y + h)
        addVertex(x, y)
        addVertex(x + w, y)
        addVertex(x + w, y + h)

        //fixme
        addIndex(0)
        addIndex(1)
        addIndex(3)

        addIndex(3)
        addIndex(1)
        addIndex(2)

        colors.add(color.r)
        colors.add(color.g)
        colors.add(color.b)
    }

    private fun addIndex(index: Int) {
        indices.add(index)
    }

    private fun addVertex(x: Float, y: Float) {
        positions.add(x)
        positions.add(y)
        positions.add(0f)
    }

    fun start() {
        if(started) throw IllegalStateException("Must end rendering before starting")
        started = true
    }

    fun end() {
        if(!started) throw IllegalStateException("Must start rendering before ending")

//        mesh.writeIndices(indices.toIntArray())
//        mesh.writeData(VertexAttribute.POSITION, positions.toFloatArray())
//        mesh.writeData(VertexAttribute.COLOR, colors.toFloatArray())

        this.render()

        indices.clear()
        positions.clear()
        colors.clear()
        started = false
    }

    private fun render() {
        shader.setUniform("projectionMatrix", camera!!.getProjectionMatrix())
        shader.setUniform("modelViewMatrix", camera!!.getModelViewMatrix(Vector2f(), 0f, Vector2f(1f, 1f), camera!!.getViewMatrix()))
        shader.bind()
        mesh.render()
        shader.unbind()
    }
}