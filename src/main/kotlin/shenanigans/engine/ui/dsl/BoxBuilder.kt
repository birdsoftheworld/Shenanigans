package shenanigans.engine.ui.dsl

import org.joml.Vector2f
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.ui.elements.Box
import shenanigans.engine.ui.elements.ColoredBox

class BoxBuilder : RecursiveUIBuilder() {
    var color: Color? = null
    var size: Vector2f? = null
    var minSize: Vector2f? = null
    var flexDirection: Box.FlexDirection? = null
    var justifyContent: Box.JustifyContent? = null
    var alignItems: Box.Align? = null
    var alignSelf: Box.Align? = null
    var flexGrow: Float? = null

    override fun buildBranch(children: List<Box>): Box {
        val box = if (color == null) {
            Box(children)
        } else {
            ColoredBox(children, color!!)
        }

        if (size != null) {
            box.setSize(size!!)
        }

        if (minSize != null) {
            box.setMinSize(minSize!!)
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