package shenanigans.engine.init

import shenanigans.engine.ecs.System

data class SystemList<T: System>(val systems: List<() -> T>, val parent: SystemList<T>? = null) {
    private fun flat() : List<() -> T> {
        return buildList {
            this.addAll(parent?.flat() ?: emptyList())
            this.addAll(systems)
        }
    }

    fun build() : List<T> = flat().map { it() }
}