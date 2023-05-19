package shenanigans.engine.ui.elements

import org.joml.Vector2f
import org.joml.Vector2fc
import org.lwjgl.util.yoga.Yoga
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.ui.dsl.UIBuilder

open class Box : AutoCloseable, UIBuilder {
    private val node = Yoga.YGNodeNew()

    private var _children = mutableListOf<Box>()
    var children: List<Box>
        get() = _children
        set(value) {
            Yoga.YGNodeRemoveAllChildren(node)

            value.forEachIndexed { index, child ->
                Yoga.YGNodeInsertChild(node, child.node, index)
            }

            _children = value.toMutableList()
        }

    override fun addChild(child: Box) {
        Yoga.YGNodeInsertChild(node, child.node, _children.size)
        _children.add(child)
    }

    override fun close() {
        Yoga.YGNodeFree(node)
        _children.forEach { child -> Yoga.YGNodeFree(child.node) }
    }

    open fun render(resources: ResourcesView, layout: Layout, z: Float) {}

    fun renderRecursive(resources: ResourcesView, parentLayout: Layout, parentZ: Float) {
        val layout = getLayout(parentLayout)
        val z = parentZ + 1f

        render(resources, layout, z)
        _children.forEach { child -> child.renderRecursive(resources, layout, z) }
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


    var size: Vector2f? = null
        set(value) {
            if (value !== null) {
                Yoga.YGNodeStyleSetWidth(node, value.x())
                Yoga.YGNodeStyleSetHeight(node, value.y())
            }
            field = value
        }

    var grow: Float = 0f
        set(value) {
            Yoga.YGNodeStyleSetFlexGrow(node, value)
            field = value
        }

    var minSize: Vector2f? = null
        set(value) {
            if (value !== null) {
                Yoga.YGNodeStyleSetMinWidth(node, value.x())
                Yoga.YGNodeStyleSetMinHeight(node, value.y())
            }
            field = value
        }

    var maxSize: Vector2f? = null
        set(value) {
            if (value !== null) {
                Yoga.YGNodeStyleSetMaxWidth(node, value.x())
                Yoga.YGNodeStyleSetMaxHeight(node, value.y())
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

    var flexDirection: FlexDirection = FlexDirection.Row
        set(value) {
            Yoga.YGNodeStyleSetDirection(
                node, when (value) {
                    FlexDirection.Row -> Yoga.YGFlexDirectionRow
                    FlexDirection.Column -> Yoga.YGFlexDirectionColumn
                    FlexDirection.RowReverse -> Yoga.YGFlexDirectionRowReverse
                    FlexDirection.ColumnReverse -> Yoga.YGFlexDirectionColumnReverse
                }
            )
            field = value
        }

    enum class FlexWrap {
        NoWrap,
        Wrap,
        WrapReverse,
    }

    var wrap: FlexWrap = FlexWrap.NoWrap
        set(value) {
            Yoga.YGNodeStyleSetFlexWrap(
                node, when (value) {
                    FlexWrap.NoWrap -> Yoga.YGWrapNoWrap
                    FlexWrap.Wrap -> Yoga.YGWrapWrap
                    FlexWrap.WrapReverse -> Yoga.YGWrapReverse
                }
            )
            field = value
        }

    enum class JustifyContent {
        Center,
        FlexStart,
        FlexEnd,
        SpaceBetween,
        SpaceAround,
        SpaceEvenly,
    }

    var justifyContent: JustifyContent = JustifyContent.FlexStart
        set(value) {
            Yoga.YGNodeStyleSetJustifyContent(
                node, when (value) {
                    JustifyContent.Center -> Yoga.YGJustifyCenter
                    JustifyContent.FlexStart -> Yoga.YGJustifyFlexStart
                    JustifyContent.FlexEnd -> Yoga.YGJustifyFlexEnd
                    JustifyContent.SpaceBetween -> Yoga.YGJustifySpaceBetween
                    JustifyContent.SpaceAround -> Yoga.YGJustifySpaceAround
                    JustifyContent.SpaceEvenly -> Yoga.YGJustifySpaceEvenly
                }
            )
            field = value
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

    var alignItems: Align = Align.Stretch
        set(value) {
            Yoga.YGNodeStyleSetAlignItems(node, value.toYoga())
            field = value
        }

    var alignSelf: Align = Align.Stretch
        set(value) {
            Yoga.YGNodeStyleSetAlignSelf(node, value.toYoga())
            field = value
        }
}