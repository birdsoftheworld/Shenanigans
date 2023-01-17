package shenanigans.engine.ui.dsl

import shenanigans.engine.ui.elements.Box

class BoxBuilder : Box(), UIBuilder<Box>, ParentUIBuilder {
    override fun addChild(child: UIBuilder<Box>) {
        addChild(child.build())
    }

    override fun build(): Box {
        return this
    }
}