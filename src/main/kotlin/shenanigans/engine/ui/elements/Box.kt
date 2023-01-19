package shenanigans.engine.ui.elements

import org.lwjgl.util.yoga.Yoga
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.ui.api.ParentUIBuilder

open class Box : ColoredNode(), ParentUIBuilder {
    /* Child Management */

    private var _children = mutableListOf<Node>()

    var children: List<Node>
        get() = _children
        set(value) {
            Yoga.YGNodeRemoveAllChildren(ygNode)

            value.forEachIndexed { index, child ->
                Yoga.YGNodeInsertChild(ygNode, child.ygNode, index)
            }

            _children = value.toMutableList()
        }

    override fun addChild(child: Node) {
        Yoga.YGNodeInsertChild(ygNode, child.ygNode, _children.size)
        _children.add(child)
    }

    override fun close() {
        super.close()
        _children.forEach { child -> Yoga.YGNodeFree(child.ygNode) }
    }

    /* Rendering */

    override fun renderIntoParent(resources: ResourcesView, parentLayout: Layout) {
        val layout = getLayout(parentLayout)
        render(resources, layout)
        _children.forEach { child -> child.renderIntoParent(resources, layout) }
    }

    /* Layout */

    enum class FlexDirection {
        Row,
        Column,
        RowReverse,
        ColumnReverse,
    }

    var flexDirection: FlexDirection = FlexDirection.Row
        set(value) {
            Yoga.YGNodeStyleSetDirection(
                ygNode, when (value) {
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
                ygNode, when (value) {
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
                ygNode, when (value) {
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
            Yoga.YGNodeStyleSetAlignItems(ygNode, value.toYoga())
            field = value
        }

    var alignSelf: Align = Align.Stretch
        set(value) {
            Yoga.YGNodeStyleSetAlignSelf(ygNode, value.toYoga())
            field = value
        }
}