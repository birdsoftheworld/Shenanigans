package shenanigans.engine.ui

import shenanigans.engine.ecs.*
import shenanigans.engine.graphics.api.RenderSystem
import shenanigans.engine.ui.elements.Box
import kotlin.reflect.KClass

class UISystem : RenderSystem {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(UIComponent::class)
    }

    override fun execute(resources: ResourcesView, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        entities.forEach {
            val ui = it.component<UIComponent>().get()
            ui.root.render(resources)
        }
    }
}

class UIComponent(var root: Box) : Component

