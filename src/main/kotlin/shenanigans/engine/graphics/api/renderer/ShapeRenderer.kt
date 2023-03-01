package shenanigans.engine.graphics.api.renderer

import org.joml.Vector2f
import shenanigans.engine.graphics.VertexAttribute
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.shader.Shader

class ShapeRenderer(vertexCapacity: Int = DEFAULT_MAX_VERTICES, indicesCapacity: Int = DEFAULT_MAX_INDICES) : AbstractRenderer(
    setOf(VertexAttribute.COLOR, VertexAttribute.ALPHA, VertexAttribute.POSITION), vertexCapacity, indicesCapacity) {

    override val shader = Shader(
        """
            #version 330

            layout (location=0) in vec3 position;
            layout (location=2) in vec3 color;
            layout (location=3) in float alpha;

            out vec4 outColor;
            
            uniform mat4 projectionMatrix;

            void main() {
                gl_Position = projectionMatrix * vec4(position, 1.0);
                outColor = vec4(color, alpha);
            }
        """.trimIndent(),
        """
            #version 330

            in vec4 outColor;
            out vec4 fragColor;

            void main() {
                fragColor = outColor;
            }
        """.trimIndent(),
    )

    init {
        createUniforms()
    }

    private val colors = ArrayList<Float>(DEFAULT_MAX_VERTICES * 3)
    private val alphas = ArrayList<Float>(DEFAULT_MAX_VERTICES)

    /**
     * draw a rectangle at `x`, `y`, `0` with size `w`, `h` of the given color, transformed by this renderer's transformation
     */
    fun rect(x: Float, y: Float, w: Float, h: Float, color: Color) = rect(x, y, 0f, w, h, color)

    /**
     * draw a rectangle at `x`, `y`, `z` with size `w`, `h` of the given color, transformed by this renderer's transformation
     */
    fun rect(x: Float, y: Float, z: Float, w: Float, h: Float, color: Color) {
        flushIfFull(4, 6)

        addIndex(0)
        addIndex(2)
        addIndex(1)

        addIndex(2)
        addIndex(3)
        addIndex(1)

        addVertex(x, y, z)
        colors.add(color.r)
        colors.add(color.g)
        colors.add(color.b)
        alphas.add(color.a)

        addVertex(x + w, y, z)
        colors.add(color.r)
        colors.add(color.g)
        colors.add(color.b)
        alphas.add(color.a)

        addVertex(x, y + h, z)
        colors.add(color.r)
        colors.add(color.g)
        colors.add(color.b)
        alphas.add(color.a)

        addVertex(x + w, y + h, z)
        colors.add(color.r)
        colors.add(color.g)
        colors.add(color.b)
        alphas.add(color.a)
    }

    /**
     * draw a convex polygon with the given vertices, transformed by this renderer's transformation
     */
    fun polygon(vertices: Array<Vector2f>, color: Color, z: Float = 0f) {
        flushIfFull(vertices.size, 3 * (vertices.size - 2))

        for (i in 1..vertices.size - 2) {
            addIndex(i)
            addIndex(i + 1)
            addIndex(0)
        }

        for (vertex in vertices) {
            addVertex(vertex.x, vertex.y, z)

            colors.add(color.r)
            colors.add(color.g)
            colors.add(color.b)
            alphas.add(color.a)
        }
    }

    override fun clearVertexAttributes() {
        colors.clear()
        alphas.clear()
    }

    override fun writeVertexAttributes() {
        mesh.writeData(VertexAttribute.COLOR, colors.toFloatArray())
        mesh.writeData(VertexAttribute.ALPHA, alphas.toFloatArray())
    }

    override fun setUniforms() {
        shader.setUniform("projectionMatrix", projection)
    }

    override fun createUniforms() {
        shader.createUniform("projectionMatrix")
    }
}