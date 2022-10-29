package shenanigans.engine.graphics.api

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector4f
import shenanigans.engine.graphics.*
import shenanigans.engine.graphics.shader.Shader
import java.lang.IllegalStateException

private const val DEFAULT_MAX_VERTICES = 500
private const val DEFAULT_MAX_INDICES = 250

class ShapeRenderer(vertexCapacity: Int, indicesCapacity: Int) {
    private val shader = Shader(
        """
            #version 330

            layout (location=0) in vec3 position;
            layout (location=2) in vec3 color;

            out vec3 outColor;
            
            uniform mat4 projectionMatrix;

            void main() {
                gl_Position = projectionMatrix * vec4(position, 1.0);
                outColor = color;
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

    private val mesh = Mesh(vertexCapacity, indicesCapacity, setOf(VertexAttribute.POSITION, VertexAttribute.COLOR))

    private var started = false

    private val indices = ArrayList<Int>(DEFAULT_MAX_INDICES)
    private val positions = ArrayList<Float>(DEFAULT_MAX_VERTICES * 3)
    private val colors = ArrayList<Float>(DEFAULT_MAX_VERTICES * 3)

    private var lowestIndex = 0

    /**
     * the projection matrix, used for projecting all vertices once `end()` is called
     */
    var projection = Matrix4f()
        set(value) { projection.set(value) }

    /**
     * the transformation matrix, used for transforming any drawing calls such as `rect()`
     */
    var transformation = Matrix4f()
        set(value) { transformation.set(value) }

    private val _temp = Vector4f()

    init {
        shader.createUniform("projectionMatrix")
    }

    /**
     * create a shape renderer with the default maximum vertices and indices
     */
    constructor() : this(DEFAULT_MAX_VERTICES, DEFAULT_MAX_INDICES)

    /**
     * draw a rectangle at `x`, `y` with size `w`, `h` of the given color, transformed by this renderer's transformation
     */
    fun rect(x: Float, y: Float, w: Float, h: Float, color: Color) {
        addIndex(0)
        addIndex(2)
        addIndex(1)

        addIndex(2)
        addIndex(3)
        addIndex(1)

        addVertex(x, y)
        colors.add(color.r)
        colors.add(color.g)
        colors.add(color.b)

        addVertex(x + w, y)
        colors.add(color.r)
        colors.add(color.g)
        colors.add(color.b)

        addVertex(x, y + h)
        colors.add(color.r)
        colors.add(color.g)
        colors.add(color.b)

        addVertex(x + w, y + h)
        colors.add(color.r)
        colors.add(color.g)
        colors.add(color.b)
    }

    /**
     * draw a convex polygon with the given vertices, transformed by this renderer's transformation
     */
    fun polygon(vertices: Array<Vector2f>, color: Color) {
        for (i in 1..vertices.size - 2) {
            addIndex(i)
            addIndex(i + 1)
            addIndex(0)
        }

        for (vertex in vertices) {
            addVertex(vertex.x, vertex.y)

            colors.add(color.r)
            colors.add(color.g)
            colors.add(color.b)
        }
    }

    private fun addIndex(index: Int) {
        indices.add(index + lowestIndex)
    }

    private fun addVertex(x: Float, y: Float) {
        _temp.set(x, y, 0f, 1f).mul(transformation)
        positions.add(_temp.x)
        positions.add(_temp.y)
        positions.add(0f)
        lowestIndex++
    }

    /**
     * start rendering
     * must be called before end()
     */
    fun start() {
        if(started) throw IllegalStateException("Must end rendering before starting")
        started = true
    }

    /**
     * stop rendering
     * must be called after start()
     */
    fun end() {
        if(!started) throw IllegalStateException("Must start rendering before ending")

        mesh.writeIndices(indices.toIntArray())
        mesh.writeData(VertexAttribute.POSITION, positions.toFloatArray())
        mesh.writeData(VertexAttribute.COLOR, colors.toFloatArray())

        this.render()

        indices.clear()
        positions.clear()
        colors.clear()
        lowestIndex = 0
        started = false
    }

    fun discard() {
        mesh.discard()
    }

    private fun render() {
        shader.bind()
        shader.setUniform("projectionMatrix", projection)
        mesh.render()
        shader.unbind()
    }
}