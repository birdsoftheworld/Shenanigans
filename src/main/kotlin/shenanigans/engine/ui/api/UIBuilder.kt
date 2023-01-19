package shenanigans.engine.ui.api

import shenanigans.engine.ui.elements.Box
import shenanigans.engine.ui.elements.Node
import shenanigans.engine.ui.elements.Text

interface UIBuilder {
    fun build(): Node
}

interface ParentUIBuilder {
    fun addChild(child: Node)
    fun addChild(fragment: Fragment) {
        fragment.items.forEach(this::addChild)
    }
}

fun ParentUIBuilder.fragment(init: Fragment.() -> Unit) {
    addChild(Fragment().apply(init))
}

fun ParentUIBuilder.box(init: Box.() -> Unit) {
    addChild(Box().apply(init))
}

fun ParentUIBuilder.text(init: Text.() -> Unit) {
    addChild(Text().apply(init))
}

fun ParentUIBuilder.text(text: String, init: Text.() -> Unit = {}) {
    addChild(Text().apply {
        this.text = text
        init()
    })
}