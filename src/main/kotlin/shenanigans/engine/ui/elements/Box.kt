package shenanigans.engine.ui.elements

import org.joml.Vector2fc
import org.lwjgl.util.yoga.Yoga

class Box(private val children: List<Box>) : AutoCloseable {
    protected val node = Yoga.YGNodeNew()

    init {
        children.forEachIndexed { index, child ->
            Yoga.YGNodeInsertChild(node, child.node, index)
        }
    }

    override fun close() {
        Yoga.YGNodeFree(node)
        children.forEach { child -> Yoga.YGNodeFree(child.node) }
    }

    fun setSize(size: Vector2fc) {
        Yoga.YGNodeStyleSetWidth(node, size.x())
        Yoga.YGNodeStyleSetHeight(node, size.y())
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

    enum class AlignItems {
        Center,
        FlexStart,
        FlexEnd,
        Stretch,
        Baseline,
    }

    fun setAlignItems(ai: AlignItems) {
        Yoga.YGNodeStyleSetAlignItems(
            node, when (ai) {
                AlignItems.Center -> Yoga.YGAlignCenter
                AlignItems.FlexStart -> Yoga.YGAlignFlexStart
                AlignItems.FlexEnd -> Yoga.YGAlignFlexEnd
                AlignItems.Stretch -> Yoga.YGAlignStretch
                AlignItems.Baseline -> Yoga.YGAlignBaseline
            }
        )
    }
}