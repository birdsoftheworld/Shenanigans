package shenanigans.demo.ui

import org.joml.Vector2f
import shenanigans.engine.ClientEngine
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.ecs.utils.AddEntitiesSystem
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.init.SystemList
import shenanigans.engine.init.client.ClientEngineOptions
import shenanigans.engine.scene.Scene
import shenanigans.engine.ui.UIRendererComponent
import shenanigans.engine.ui.UISystem
import shenanigans.engine.ui.api.*
import shenanigans.engine.ui.elements.Box
import shenanigans.engine.ui.elements.Node

class Counter : UI {
    private val count = 0

    override fun render(): Fragment.() -> Unit {
        return {
            box {
                flexDirection = Box.FlexDirection.Column
                justifyContent = Box.JustifyContent.Center
                alignItems = Box.Align.Center

                color = Color(0.1f, 0.1f, 0.1f)

                text {
                    text = "Count: $count"

                    color = Color(1f, 1f, 1f)
                    fontSize = 48f
                }
            }
        }
    }
}


