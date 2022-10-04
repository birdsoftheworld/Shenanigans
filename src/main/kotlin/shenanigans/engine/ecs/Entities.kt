package shenanigans.engine.ecs

import kotlin.reflect.KClass

class Entities constructor(val componentOrder: List<KClass<Component>>) {
    private val entities: MutableList<Array<Component?>> = arrayListOf()

    fun runSystem(system: System) {
        system.run(EntityIterator(componentOrder, entities))
    }
}

private class EntityIterator(val query: Iterable<KClass<Component>>, val components: List<Array<Component?>>) :
    AbstractIterator<Array<Component?>>() {
    var pos: Int = 0

    override fun computeNext() {
        while (pos < components.size) {
            val component = components[pos]

            if (query.zip(component).all {
                it.first.isInstance(it.second)
            }) {
                setNext(component.toList().toTypedArray())
                break
            } else {
                pos++
            }
        }
        done()
    }
}