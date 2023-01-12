package shenanigans.engine.ui.dsl

import shenanigans.engine.ui.elements.Box

class BoxBuilder : Box(), UIBuilder<Box>, ParentUIBuilder {
    private val deferredChildren = mutableListOf<UIBuilder<Box>>()

    override fun addChild(child: UIBuilder<Box>) {
        deferredChildren.add(child)
    }

    override fun build(): Box {
        children = deferredChildren.map(UIBuilder<Box>::build)
        return this
    }
}