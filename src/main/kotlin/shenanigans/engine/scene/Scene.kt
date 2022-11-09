package shenanigans.engine.scene

import shenanigans.engine.ecs.*

class Scene {
    private val entities: Entities = Entities()
    var defaultSystems = mutableListOf<System>()

    /**
     * run the default systems with default resources
     */
    fun runSystems(resources: Resources) {
        runSystems(resources, defaultSystems)
    }

    /**
     * run the specified systems with the specified resources
     */
    fun runSystems(resources: Resources, systems: List<System>) {
        systems.forEach {
            entities.runSystem(it, resources)
        }
    }
}