package shenanigans.engine.ui.elements

import org.joml.Vector2f
import org.joml.Vector2fc
import org.lwjgl.util.yoga.Yoga
import shenanigans.engine.ecs.ResourcesView

open class Node : AutoCloseable {
    internal val ygNode = Yoga.YGNodeNew()


    override fun close() {
        Yoga.YGNodeFree(ygNode)
    }

    /* Rendering */

    open fun render(resources: ResourcesView, layout: Layout) {}

    open fun renderIntoParent(resources: ResourcesView, parentLayout: Layout) {
        val layout = getLayout(parentLayout)
        render(resources, layout)
    }

    /* Layout */

    data class Layout(val position: Vector2f, val size: Vector2f)

    fun getLayout(parent: Layout): Layout {
        val ret = getLayoutAsRoot()
        ret.position.add(parent.position)
        return ret
    }

    private fun getLayoutAsRoot(): Layout {
        return Layout(
            Vector2f(Yoga.YGNodeLayoutGetLeft(ygNode), Yoga.YGNodeLayoutGetTop(ygNode)),
            Vector2f(Yoga.YGNodeLayoutGetWidth(ygNode), Yoga.YGNodeLayoutGetHeight(ygNode))
        )
    }

    fun computeLayout(size: Vector2fc) {
        Yoga.YGNodeCalculateLayout(ygNode, size.x(), size.y(), Yoga.YGDirectionLTR)
    }

    var size: Vector2f? = null
        set(value) {
            if (value !== null) {
                Yoga.YGNodeStyleSetWidth(ygNode, value.x())
                Yoga.YGNodeStyleSetHeight(ygNode, value.y())
            }
            field = value
        }

    var grow: Float = 0f
        set(value) {
            Yoga.YGNodeStyleSetFlexGrow(ygNode, value)
            field = value
        }

    var minSize: Vector2f? = null
        set(value) {
            if (value !== null) {
                Yoga.YGNodeStyleSetMinWidth(ygNode, value.x())
                Yoga.YGNodeStyleSetMinHeight(ygNode, value.y())
            }
            field = value
        }

    var maxSize: Vector2f? = null
        set(value) {
            if (value !== null) {
                Yoga.YGNodeStyleSetMaxWidth(ygNode, value.x())
                Yoga.YGNodeStyleSetMaxHeight(ygNode, value.y())
            }
            field = value
        }

    enum class Edge {
        All,
        Horizontal, Vertical,
        Left, Top, Right, Bottom,
    }

    fun setMargin(edge: Edge, margin: Float) {
        Yoga.YGNodeStyleSetMargin(
            ygNode,
            when (edge) {
                Edge.All -> Yoga.YGEdgeAll
                Edge.Horizontal -> Yoga.YGEdgeHorizontal
                Edge.Vertical -> Yoga.YGEdgeVertical
                Edge.Left -> Yoga.YGEdgeLeft
                Edge.Top -> Yoga.YGEdgeTop
                Edge.Right -> Yoga.YGEdgeRight
                Edge.Bottom -> Yoga.YGEdgeBottom
            },
            margin
        )
    }

    fun setPadding(edge: Edge, padding: Float) {
        Yoga.YGNodeStyleSetPadding(
            ygNode,
            when (edge) {
                Edge.All -> Yoga.YGEdgeAll
                Edge.Horizontal -> Yoga.YGEdgeHorizontal
                Edge.Vertical -> Yoga.YGEdgeVertical
                Edge.Left -> Yoga.YGEdgeLeft
                Edge.Top -> Yoga.YGEdgeTop
                Edge.Right -> Yoga.YGEdgeRight
                Edge.Bottom -> Yoga.YGEdgeBottom
            },
            padding
        )
    }
}