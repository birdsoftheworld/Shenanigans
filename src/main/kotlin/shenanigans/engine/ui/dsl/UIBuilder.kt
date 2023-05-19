package shenanigans.engine.ui.dsl

import shenanigans.engine.ui.elements.Box
import shenanigans.engine.ui.elements.ColoredBox
import shenanigans.engine.ui.elements.Text

interface UIBuilder {
    fun addChild(child: Box)

    fun box(init: Box.() -> Unit) {
        addChild(Box().apply(init))
    }

    fun coloredBox(init: ColoredBox.() -> Unit) {
        addChild(ColoredBox().apply(init))
    }

    fun text(init: Text.() -> Unit) {
        addChild(Text().apply(init))
    }

    fun text(text: String, init: Text.() -> Unit = {}) {
        text {
            this.text = text
            init()
        }
    }
}

fun buildUI(init: UIBuilder.() -> Unit): Box {
    return Box().apply(init)
}