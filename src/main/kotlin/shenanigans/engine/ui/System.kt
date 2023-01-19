package shenanigans.engine.ui

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.graphics.api.RenderSystem
import shenanigans.engine.ui.elements.Box
import shenanigans.engine.ui.elements.Node
import shenanigans.engine.util.toFloat
import shenanigans.engine.window.WindowResource
import kotlin.reflect.KClass

class UISystem : RenderSystem {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(UIComponent::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val window = resources.get<WindowResource>().window

        entities.forEach {
            val ui = it.component<UIComponent>().get()
            ui.root.computeLayout(window.size.toFloat())
            ui.root.renderIntoParent(resources, Node.Layout(Vector2f(), window.size.toFloat()))
        }
    }
}

class UIComponent(var root: Box) : Component

