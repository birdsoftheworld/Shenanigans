package shenanigans.engine.ecs

import kotlin.reflect.KClass

interface System {
    fun query(): Iterable<KClass<Component>>
    fun run(entities: Iterator<Array<Component?>>)
}
