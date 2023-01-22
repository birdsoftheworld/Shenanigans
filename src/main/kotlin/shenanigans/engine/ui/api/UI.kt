package shenanigans.engine.ui.api

interface UI {
    fun render(): Fragment.() -> Unit
}