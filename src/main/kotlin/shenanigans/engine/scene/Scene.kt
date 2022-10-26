package shenanigans.engine.scene

import shenanigans.engine.ecs.Entities
import shenanigans.engine.ecs.Resources
import shenanigans.engine.ecs.System
import shenanigans.engine.resources.DeltaTime

class Scene {
    private val entities : Entities = Entities()
    private val systems = mutableListOf<System>()

    fun runSystems(deltaTime : DeltaTime) {
        val resources = Resources()
        resources.set(deltaTime)

        systems.forEach() {
            entities.runSystem(it, resources)
        }
    }
}