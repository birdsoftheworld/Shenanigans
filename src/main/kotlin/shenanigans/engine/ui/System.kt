package shenanigans.engine.ui

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.graphics.api.RenderSystem
import shenanigans.engine.ui.api.Fragment
import shenanigans.engine.ui.api.UIComponent
import shenanigans.engine.ui.elements.Node
import shenanigans.engine.util.toFloat
import shenanigans.engine.window.WindowResource
import kotlin.reflect.KClass

class UISystem : RenderSystem {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(UIRendererComponent::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val window = resources.get<WindowResource>().window

        entities.forEach {
            val ui = it.component<UIRendererComponent>().get()

            val fragment = Fragment()
            fragment.apply(ui.component.render())
            fragment.build().use { node ->
                node.computeLayout(window.size.toFloat())
                node.renderIntoParent(resources, Node.Layout(Vector2f(), window.size.toFloat()))
            }
        }
    }
}

class UIRendererComponent(val component: UIComponent) : Component

