package shenanigans.engine.scene

import shenanigans.engine.ecs.Entities
import shenanigans.engine.ecs.Resources
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.ecs.System

class Scene {
    private val entities: Entities = Entities()
    var defaultSystems = mutableListOf<System>()
    val sceneResources = Resources()

    /**
     * run the default systems
     */
    fun runSystems(resources: ResourcesView) {
        runSystems(resources, defaultSystems)
    }

    /**
     * run the specified systems with the specified resources
     */
    fun runSystems(resources: ResourcesView, systems: List<System>) {
        systems.forEach {
            entities.runSystem(it, resources)
        }
    }
}