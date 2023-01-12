package shenanigans.engine.ui.dsl

import shenanigans.engine.ui.elements.Box
import shenanigans.engine.ui.elements.ColoredBox

class ColoredBoxBuilder: ColoredBox(), UIBuilder<ColoredBox>, ParentUIBuilder {
    private val deferredChildren = mutableListOf<UIBuilder<Box>>()

    override fun addChild(child: UIBuilder<Box>) {
        deferredChildren.add(child)
    }

    override fun build(): ColoredBox {
        children = deferredChildren.map(UIBuilder<Box>::build)
        return this
    }
}