package shenanigans.engine.ecs

import kotlin.reflect.KClass

interface System {
    /**
     * Returns a collection of component classes on which the system operates.
     */
    fun query(): Iterable<KClass<out Component>>

    /**
     * Execute the system at physics time.
     */
    fun executePhysics(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {}

    /**
     * Execute the system at network time.
     */
    fun executeNetwork(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {}

    /**
     * Execute the system at render time.
     */
    fun executeRender(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {}
}
