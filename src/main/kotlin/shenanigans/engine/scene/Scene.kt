package shenanigans.engine.scene

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.EventQueues

data class Scene(
    val entities: Entities = Entities(),
    var defaultSystems: MutableList<System> = mutableListOf(),
    val sceneResources: Resources = Resources()
) {
    fun <S : System, Q : EventQueue> runSystem(
        execute: S.(ResourcesView, EventQueues<Q>, EntitiesView, EntitiesLifecycle) -> Unit,
        resources: ResourcesView,
        eventQueues: EventQueues<Q>,
    ): (S) -> Unit {
        return { system -> entities.runSystem(execute, system, resources, eventQueues) }
    }
}