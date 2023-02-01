package shenanigans.engine.scene

import shenanigans.engine.ecs.*

data class Scene(
    val entities: Entities = Entities(),
    var defaultSystems: MutableList<System> = mutableListOf(),
    val sceneResources: Resources = Resources()
) {
    fun <S : System> runSystem(
        execute: S.(ResourcesView, EntitiesView, EntitiesLifecycle) -> Unit,
        resources: ResourcesView,
    ): (S) -> Unit {
        return { system -> entities.runSystem(execute, system, resources) }
    }
}