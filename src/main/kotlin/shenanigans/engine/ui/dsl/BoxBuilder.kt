package shenanigans.engine.ui.dsl

import shenanigans.engine.ui.elements.Box
import shenanigans.engine.ui.elements.Node

class BoxBuilder : Box(), UIBuilder<Box>, ParentUIBuilder {
    override fun addChild(child: UIBuilder<Node>) {
        addChild(child.build())
    }

    override fun build(): Box {
        return this
    }
}