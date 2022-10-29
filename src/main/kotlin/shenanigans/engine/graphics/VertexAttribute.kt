package shenanigans.engine.graphics

private const val POSITION_INDEX = 0
private const val TEX_COORDS_INDEX = 1
private const val COLORS_INDEX = 2

/**
 * defines a vertex attribute to be used with a `Mesh`
 */
class VertexAttribute private constructor(val name: String, val typeSize: Int, val attributeSize: Int, val index: Int) {
    companion object {
        val POSITION = VertexAttribute("position", Float.SIZE_BYTES, 3, POSITION_INDEX)
        val TEX_COORDS = VertexAttribute("texCoord", Float.SIZE_BYTES, 2, TEX_COORDS_INDEX)
        val COLOR = VertexAttribute("color", Float.SIZE_BYTES, 3, COLORS_INDEX)
    }
}