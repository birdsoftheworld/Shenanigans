package shenanigans.demo.ui

import org.joml.Vector2f
import shenanigans.engine.ClientEngine
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.ecs.utils.AddEntitiesSystem
import shenanigans.engine.events.EventQueue
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.init.SystemList
import shenanigans.engine.init.client.ClientEngineOptions
import shenanigans.engine.scene.Scene
import shenanigans.engine.ui.UIRendererComponent
import shenanigans.engine.ui.UISystem
import shenanigans.engine.ui.api.*
import shenanigans.engine.ui.elements.Box
import shenanigans.engine.ui.elements.Node
import shenanigans.engine.window.MouseButtonAction
import shenanigans.engine.window.events.MouseButtonEvent
import shenanigans.engine.window.events.MouseState

class Counter : UI {
    private var count = 0

    override fun render(): Fragment.() -> Unit {
        return {
            box {
                flexDirection = Box.FlexDirection.Column
                justifyContent = Box.JustifyContent.Center
                alignItems = Node.Align.Center

                color = Color(0.1f, 0.1f, 0.1f)

                text {
                    text = "Count: $count"

                    color = Color(1f, 1f, 1f)
                    fontSize = 48f

                    setMargin(Node.Edge.All, 20f)
                }

                button("decrement") { count -= 1; }
                button("increment") { count += 1; }
            }
        }
    }
}


internal fun ParentUIBuilder.button(text: String, onClick: () -> Unit) {
    box {
        onEvents = { resources ->
            val mouse = resources.get<MouseState>()

            if (layout.contains(mouse.position())) {
                val events = resources.get<EventQueue>()

                events.iterate<MouseButtonEvent>().forEach { event ->
                    if (event.action == MouseButtonAction.PRESS) {
                        onClick()
                    }
                }
            }
        }

        justifyContent = Box.JustifyContent.Center
        alignItems = Node.Align.Center

        setPadding(Node.Edge.All, 15f)
        setMargin(Node.Edge.All, 10f)

        color = Color(0.1f, 0.2f, 0.8f)

        text {
            this.text = text
            color = Color(1f, 1f, 1f)
        }
    }
}