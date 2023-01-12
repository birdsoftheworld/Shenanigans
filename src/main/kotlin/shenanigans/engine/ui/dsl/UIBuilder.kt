package shenanigans.engine.ui.dsl

import shenanigans.engine.ui.elements.Box

sealed class UIBuilder {
    abstract fun build(): Box
}

sealed class RecursiveUIBuilder : UIBuilder() {
    private val children = mutableListOf<UIBuilder>()

    abstract fun buildBranch(children: List<Box>): Box

    override fun build(): Box {
        return buildBranch(children.map { it.build() })
    }

    fun box(init: BoxBuilder.() -> Unit) {
        children.add(BoxBuilder().apply(init))
    }
}

fun buildUI(init: BoxBuilder.() -> Unit): Box {
    return BoxBuilder().apply(init).build()
}