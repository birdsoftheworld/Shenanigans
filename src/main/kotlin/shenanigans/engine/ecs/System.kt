package shenanigans.engine.ecs

import kotlin.reflect.KClass

interface System {
    fun query(): Iterable<KClass<out Component>>
    fun run(entities: Iterator<EntityView>)
}
