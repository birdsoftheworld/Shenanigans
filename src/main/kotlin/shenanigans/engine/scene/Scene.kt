package shenanigans.engine.scene

import shenanigans.engine.ecs.*
import shenanigans.engine.physics.CollisionSystem

class Scene {
    private val entities = Entities()
    private val systems = mutableListOf<System>()

    init {
        systems.add(CollisionSystem())
    }

    /**
     * run the default systems with default resources
     */
    fun runSystems(resources: Resources) {
        systems.forEach {
            entities.runSystem(it, resources)
        }
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