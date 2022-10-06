package shenanigans.engine.ecs

import kotlin.reflect.KClass

class Entities {
    private val entities: MutableList<Map<KClass<out Component>, Component>> = arrayListOf()

    fun runSystem(system: System) {
        val query = system.query().toSet()
        system.run(entities.filter { cs ->
            cs.keys.containsAll(query)
        }.map { EntityView(it) }.iterator())
    }
}

class EntityView(val map: Map<KClass<out Component>, Component>) {
    inline fun <reified T : Component> getComponent() {
        map[T::class]
    }
}