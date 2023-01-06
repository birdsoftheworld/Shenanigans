package shenanigans.engine.ui.elements

import org.joml.Vector2f
import org.joml.Vector2fc
import org.lwjgl.util.yoga.Yoga
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.window.WindowResource

open class Box(val children: List<Box>) : AutoCloseable {
    private val node = Yoga.YGNodeNew()

    init {
        children.forEachIndexed { index, child ->
            Yoga.YGNodeInsertChild(node, child.node, index)
        }
    }

    open fun render(resources: ResourcesView, layout: Layout) {}

    override fun close() {
        Yoga.YGNodeFree(node)
        children.forEach { child -> Yoga.YGNodeFree(child.node) }
    }


    fun renderRecursive(resources: ResourcesView, parentLayout: Layout) {
        val layout = getLayout(parentLayout)
        render(resources, layout)
        children.forEach { child -> child.renderRecursive(resources, layout) }
    }

    data class Layout(val position: Vector2f, val size: Vector2f)

    private fun getLayoutAsRoot(): Layout {
        return Layout(
            Vector2f(Yoga.YGNodeLayoutGetLeft(node), Yoga.YGNodeLayoutGetTop(node)),
            Vector2f(Yoga.YGNodeLayoutGetWidth(node), Yoga.YGNodeLayoutGetHeight(node))
        )
    }

    private fun getLayout(parent: Layout): Layout {
        val ret = getLayoutAsRoot()
        ret.position.add(parent.position)
        return ret
    }


    fun computeLayout(size: Vector2fc) {
        Yoga.YGNodeCalculateLayout(node, size.x(), size.y(), Yoga.YGDirectionLTR)
    }

    fun setSize(size: Vector2fc) {
        Yoga.YGNodeStyleSetWidth(node, size.x())
        Yoga.YGNodeStyleSetHeight(node, size.y())
    }

    fun setGrow(grow: Float) {
        Yoga.YGNodeStyleSetFlexGrow(node, grow)
    }

    fun setGrow() {
        setGrow(1f)
    }

    fun setMinSize(size: Vector2fc) {
        Yoga.YGNodeStyleSetMinWidth(node, size.x())
        Yoga.YGNodeStyleSetMinHeight(node, size.y())
    }

    fun setMaxSize(size: Vector2fc) {
        Yoga.YGNodeStyleSetMaxWidth(node, size.x())
        Yoga.YGNodeStyleSetMaxHeight(node, size.y())
    }

    enum class Edge {
        All,
        Horizontal, Vertical,
        Left, Top, Right, Bottom,
    }

    fun setMargin(edge: Edge, margin: Float) {
        Yoga.YGNodeStyleSetMargin(
            node,
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
            node,
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

    enum class FlexDirection {
        Row,
        Column,
        RowReverse,
        ColumnReverse,
    }

    fun setFlexDirection(direction: FlexDirection) {
        Yoga.YGNodeStyleSetDirection(
            node, when (direction) {
                FlexDirection.Row -> Yoga.YGFlexDirectionRow
                FlexDirection.Column -> Yoga.YGFlexDirectionColumn
                FlexDirection.RowReverse -> Yoga.YGFlexDirectionRowReverse
                FlexDirection.ColumnReverse -> Yoga.YGFlexDirectionColumnReverse
            }
        )
    }

    enum class FlexWrap {
        NoWrap,
        Wrap,
        WrapReverse,
    }

    fun setFlexWrap(wrap: FlexWrap) {
        Yoga.YGNodeStyleSetFlexWrap(
            node, when (wrap) {
                FlexWrap.NoWrap -> Yoga.YGWrapNoWrap
                FlexWrap.Wrap -> Yoga.YGWrapWrap
                FlexWrap.WrapReverse -> Yoga.YGWrapReverse
            }
        )
    }

    enum class JustifyContent {
        Center,
        FlexStart,
        FlexEnd,
        SpaceBetween,
        SpaceAround,
        SpaceEvenly,
    }

    fun setJustifyContent(jc: JustifyContent) {
        Yoga.YGNodeStyleSetJustifyContent(
            node, when (jc) {
                JustifyContent.Center -> Yoga.YGJustifyCenter
                JustifyContent.FlexStart -> Yoga.YGJustifyFlexStart
                JustifyContent.FlexEnd -> Yoga.YGJustifyFlexEnd
                JustifyContent.SpaceBetween -> Yoga.YGJustifySpaceBetween
                JustifyContent.SpaceAround -> Yoga.YGJustifySpaceAround
                JustifyContent.SpaceEvenly -> Yoga.YGJustifySpaceEvenly
            }
        )
    }

    enum class Align {
        Center,
        FlexStart,
        FlexEnd,
        Stretch,
        Baseline;

        fun toYoga(): Int {
            return when (this) {
                Center -> Yoga.YGAlignCenter
                FlexStart -> Yoga.YGAlignFlexStart
                FlexEnd -> Yoga.YGAlignFlexEnd
                Stretch -> Yoga.YGAlignStretch
                Baseline -> Yoga.YGAlignBaseline
            }
        }
    }

    fun setAlignItems(align: Align) {
        Yoga.YGNodeStyleSetAlignItems(
            node, align.toYoga()
        )
    }

    fun setAlignSelf(align: Align) {
        Yoga.YGNodeStyleSetAlignSelf(
            node, align.toYoga()
        )
    }
}