package shenanigans.engine.ui.dsl

import shenanigans.engine.ui.elements.Box

sealed interface UIBuilder<out T> {
    fun build(): T
}

sealed interface ParentUIBuilder {
    fun addChild(child: UIBuilder<Box>)
}

fun ParentUIBuilder.box(init: BoxBuilder.() -> Unit) {
    addChild(BoxBuilder().apply(init))
}

fun ParentUIBuilder.coloredBox(init: ColoredBoxBuilder.() -> Unit) {
    addChild(ColoredBoxBuilder().apply(init))
}

fun ParentUIBuilder.text(init: TextBuilder.() -> Unit) {
    addChild(TextBuilder().apply(init))
}

fun ParentUIBuilder.text(text: String, init: TextBuilder.() -> Unit = {}) {
    addChild(TextBuilder().apply {
        this.text = text
        init()
    })
}

fun buildUI(init: BoxBuilder.() -> Unit): Box {
    return BoxBuilder().apply(init).build()
}