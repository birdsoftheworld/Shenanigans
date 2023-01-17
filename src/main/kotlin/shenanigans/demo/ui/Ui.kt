package shenanigans.demo.ui

import org.joml.Vector2f
import shenanigans.engine.ClientEngine
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.ecs.utils.AddEntitiesSystem
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.init.SystemList
import shenanigans.engine.init.client.ClientEngineOptions
import shenanigans.engine.scene.Scene
import shenanigans.engine.ui.UIComponent
import shenanigans.engine.ui.UISystem
import shenanigans.engine.ui.dsl.buildUI
import shenanigans.engine.ui.elements.Box

fun main() {
    ClientEngine(makeScene(), ClientEngineOptions(SystemList(listOf { UISystem() }))).run()
}

fun makeScene(): Scene {
    val scene = Scene()

    val ui = buildUI {
        coloredBox {
            minSize = Vector2f(100f, 100f)

            color = Color(0.5f, 0.5f, 0.5f)
        }

        box {
            grow = 1f
            flexDirection = Box.FlexDirection.Column
            justifyContent = Box.JustifyContent.FlexStart

            coloredBox {
                color = Color(1f, 0f, 0f)
                size = Vector2f(200f, 100f)
            }

            coloredBox {
                color = Color(0f, 1f, 0f)
                minSize = Vector2f(100f, 100f)
                grow = 1f
            }

            coloredBox {
                color = Color(0f, 0f, 1f)
                size = Vector2f(200f, 300f)
                alignSelf = Box.Align.FlexEnd
            }
        }
    }

    scene.runSystems(
        ResourcesView(), listOf(
            AddEntitiesSystem(
                sequenceOf(
                    sequenceOf(
                        UIComponent(
                            ui
                        )
                    )
                )
            )
        )
    )

    return scene
}