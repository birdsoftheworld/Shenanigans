package shenanigans.engine.ui

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.graphics.api.RenderSystem
import shenanigans.engine.ui.api.Fragment
import shenanigans.engine.ui.api.UI
import shenanigans.engine.util.toFloat
import shenanigans.engine.window.WindowResource
import shenanigans.engine.window.events.MouseEvent
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

            // recursively layout the tree
            node.computeLayout(window.size.toFloat())

            // recursively handle events
            node.handleEvents(resources)

            // recursively render the tree
            node.render(resources)

            // handle mouse events
            events.iterate<MouseEvent>().forEach {

            }


            node.close()
        }
    }
}

class UIRendererComponent(val component: UI) : Component

