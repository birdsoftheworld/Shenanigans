package shenanigans.engine.graphics.api.renderer

import org.joml.Vector2f
import shenanigans.engine.graphics.VertexAttribute
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.shader.Shader

class ShapeRenderer(vertexCapacity: Int = DEFAULT_MAX_VERTICES, indicesCapacity: Int = DEFAULT_MAX_INDICES) : AbstractRenderer(
    setOf(VertexAttribute.COLOR, VertexAttribute.POSITION), vertexCapacity, indicesCapacity) {

    override val shader = Shader(
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

    init {
        createUniforms()
    }

    private val colors = ArrayList<Float>(DEFAULT_MAX_VERTICES * 3)

    /**
     * draw a rectangle at `x`, `y` with size `w`, `h` of the given color, transformed by this renderer's transformation
     */
    fun rect(x: Float, y: Float, w: Float, h: Float, color: Color) {
        flushIfFull(4, 6)

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
        flushIfFull(vertices.size, 3 * (vertices.size - 2))

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

    override fun clearVertexAttributes() {
        colors.clear()
    }

    override fun writeVertexAttributes() {
        mesh.writeData(VertexAttribute.COLOR, colors.toFloatArray())
    }

    override fun setUniforms() {
        shader.setUniform("projectionMatrix", projection)
    }

    override fun createUniforms() {
        shader.createUniform("projectionMatrix")
    }
}