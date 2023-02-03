package shenanigans.engine.ecs.utils

import shenanigans.engine.ecs.*
import kotlin.reflect.KClass

class AddEntitiesSystem(val entities: Sequence<Sequence<Component>>) : System {
    override fun query(): Iterable<KClass<out Component>> {
        return emptySet()
    }

    override fun executePhysics(resources: ResourcesView, _entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        entities.forEach { lifecycle.add(it) }
    }
}