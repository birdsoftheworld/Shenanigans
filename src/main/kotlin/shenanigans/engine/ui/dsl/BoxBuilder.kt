package shenanigans.engine.ui.dsl

import org.joml.Vector2f
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.ui.elements.Box

class BoxBuilder : RecursiveUIBuilder() {
    val box = Box(listOf())

    var color: Color? = null
    var size: Vector2f? = null
    var minSize: Vector2f? = null
    var maxSize: Vector2f? = null
    var flexDirection: Box.FlexDirection? = null
    var justifyContent: Box.JustifyContent? = null
    var alignItems: Box.Align? = null
    var alignSelf: Box.Align? = null
    var flexGrow: Float? = null

    fun margin(edge: Box.Edge, value: Float) {
        box.setMargin(edge, value)
    }

    fun padding(edge: Box.Edge, value: Float) {
        box.setPadding(edge, value)
    }

    override fun buildBranch(children: List<Box>): Box {
        box.setChildren(children)

        box.color = color

        if (size != null) {
            box.setSize(size!!)
        }

        if (minSize != null) {
            box.setMinSize(minSize!!)
        }

        if (maxSize != null) {
            box.setMaxSize(maxSize!!)
        }

        if (flexDirection != null) {
            box.setFlexDirection(flexDirection!!)
        }

        if (justifyContent != null) {
            box.setJustifyContent(justifyContent!!)
        }

        if (alignItems != null) {
            box.setAlignItems(alignItems!!)
        }

        if (alignSelf != null) {
            box.setAlignSelf(alignSelf!!)
        }

        if (flexGrow != null) {
            box.setGrow(flexGrow!!)
        }

        return box
    }
}