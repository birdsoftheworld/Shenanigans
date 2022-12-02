package shenanigans.engine.ui

import shenanigans.engine.ecs.*
import shenanigans.engine.ui.elements.Box
import kotlin.reflect.KClass

class UISystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(UIComponent::class)
    }

    override fun execute(resources: ResourcesView, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        TODO("Not yet implemented")
    }
}

class UIComponent(var ui: Box) : Component

