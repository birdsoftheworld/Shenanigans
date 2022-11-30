package shenanigans.engine.ecs

import kotlin.reflect.KClass

interface System {
    /**
     * Returns a collection of component classes on which the system operates.
     */
    fun query(): Iterable<KClass<out Component>>

    /**
     * Execute the system on the entities.
     */
    fun execute(resources: ResourcesView, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle)
}
