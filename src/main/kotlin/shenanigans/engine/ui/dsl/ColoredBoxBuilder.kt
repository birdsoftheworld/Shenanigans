package shenanigans.engine.ui.dsl

import shenanigans.engine.ui.elements.Box
import shenanigans.engine.ui.elements.ColoredBox

class ColoredBoxBuilder: ColoredBox(), UIBuilder<ColoredBox>, ParentUIBuilder {
    override fun addChild(child: UIBuilder<Box>) {
        addChild(child.build())
    }

    override fun build(): ColoredBox {
        return this
    }
}