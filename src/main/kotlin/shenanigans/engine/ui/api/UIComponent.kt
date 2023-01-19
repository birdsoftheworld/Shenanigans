package shenanigans.engine.ui.api

interface UIComponent {
    fun render(): Fragment.() -> Unit
}