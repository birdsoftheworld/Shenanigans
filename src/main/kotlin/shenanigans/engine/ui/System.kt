package shenanigans.engine.ui

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.graphics.api.RenderSystem
import shenanigans.engine.ui.api.Fragment
import shenanigans.engine.ui.api.UI
import shenanigans.engine.ui.elements.Node
import shenanigans.engine.util.toFloat
import shenanigans.engine.window.WindowResource
import shenanigans.engine.window.events.MouseEvent
import shenanigans.engine.window.events.MouseState
import kotlin.reflect.KClass

class UISystem : RenderSystem {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(UIRendererComponent::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val window = resources.get<WindowResource>().window
        val events = resources.get<EventQueue>()

        entities.forEach {
            val ui = it.component<UIRendererComponent>().get()

            // build into a tree of Nodes
            val fragment = Fragment()
            fragment.apply(ui.component.render())
            val node = fragment.build()

            // recursively layout the tree, then render it
            node.computeLayout(window.size.toFloat())
            node.render(resources)

            // handle mouse events
            events.iterate<MouseEvent>().forEach {

            }


            node.close()
        }
    }
}

class UIRendererComponent(val component: UI) : Component

