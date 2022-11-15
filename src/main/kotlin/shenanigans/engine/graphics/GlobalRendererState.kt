package shenanigans.engine.graphics

object GlobalRendererState {
    var initialized = false
        internal set

    var renderThread: Thread? = null
        internal set

    fun isInitializedAndOnRenderThread(): Boolean {
        return initialized && renderThread == Thread.currentThread()
    }

    internal fun initialize() {
        require(!initialized)
        initialized = true
        renderThread = Thread.currentThread()
    }
}