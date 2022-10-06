package shenanigans.engine.ecs

import kotlin.reflect.KClass

class Entities constructor(val componentOrder: List<KClass<Component>>) {
    private val entities: MutableList<Array<Component?>> = arrayListOf()

    fun runSystem(system: System) {
        val query = system.query().toSet()
        val orderedQuery = componentOrder.map {
            if (query.contains(it)) {
                it
            } else {
                null
            }
        }

        system.run(EntityIterator(orderedQuery, entities))
    }
}

private class EntityIterator(val query: Iterable<KClass<Component>?>, val entities: List<Array<Component?>>) :
    AbstractIterator<Array<Component?>>() {
    var pos: Int = 0

    override fun computeNext() {
        while (pos < entities.size) {
            val entity = entities[pos]

            if (query.zip(entity).all {
                    it.first !== null && it.first!!.isInstance(it.second)
                }) {
                setNext(entity.toList().toTypedArray())
                break
            } else {
                pos++
            }
        }
        done()
    }
}