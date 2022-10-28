package shenanigans.engine.scene

import shenanigans.engine.ecs.*

class Scene {
    private val entities = Entities()
    private val systems = mutableListOf<System>()

    val resources = Resources()

    inline fun <reified T : Resource> getResource() : T {
        return resources.get()
    }

    inline fun <reified T : Resource> setResource(resource: T) {
        resources.set(resource)
    }

    fun runSystems() {
        systems.forEach {
            entities.runSystem(it, resources)
        }
    }

    fun runSystems(resources: Resources, systems: List<System>) {
        systems.forEach {
            entities.runSystem(it, resources)
        }
    }
}