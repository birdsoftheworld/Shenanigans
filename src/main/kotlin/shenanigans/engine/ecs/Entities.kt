package shenanigans.engine.ecs

import kotlin.reflect.KClass

class Entities constructor(val componentOrder: List<KClass<Component>>) {
    private val components: MutableList<Array<Component?>> = arrayListOf()

    fun runSystem(system: System) {

    }
}

private class EntityIterator(val query: Iterable<KClass<Component>>, val components: List<Array<Component?>>) :
    AbstractIterator<List<Component?>>() {
    var pos: Int = 0

    override fun computeNext() {
        while (pos < components.size) {
            val component = components[pos]

            if (query.zip(component).all {
                it.first.isInstance(it.second)
            }) {
                setNext(component.toList())
                break
            } else {
                pos++
            }
        }
        done()
    }
}