package shenanigans.engine.ui.dsl

import shenanigans.engine.ui.elements.Text

class TextBuilder : Text(), UIBuilder<Text> {
    override fun build(): Text {
        return this
    }
}