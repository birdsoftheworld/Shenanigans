package shenanigans.engine.ecs

import org.joml.Vector2d
import org.joml.Vector2f
import shenanigans.engine.ecs.components.Collider
import shenanigans.engine.ecs.components.Shape
import shenanigans.engine.ecs.components.Transform
import kotlin.reflect.KClass

class Entities {
    private val entities: MutableList<Map<KClass<out Component>, Component>> = arrayListOf()

    fun runSystem(system: System) {
        val query = system.query().toSet()
        system.run(entities.filter { cs ->
            cs.keys.containsAll(query)
        }.map { EntityView(it) }.iterator())
    }

    init {
        val map = mutableMapOf<KClass<out Component>, Component>()
        map[Transform::class] = Transform(Vector2f(10f, 10f), 0f, Vector2f(1f, 1f))
        map[Shape::class] = Shape(arrayOf(Vector2f(0f, 0f), Vector2f(10f, 0f), Vector2f(10f, 10f), Vector2f(0f, 10f)))
        map[Collider::class] = Collider(map[Shape::class] as Shape, static = false, triggerCollider = false)
        entities.add(map)
    }
}

class EntityView(val map: Map<KClass<out Component>, Component>) {
    inline fun <reified T : Component> getComponent() {
        map[T::class]
    }
}