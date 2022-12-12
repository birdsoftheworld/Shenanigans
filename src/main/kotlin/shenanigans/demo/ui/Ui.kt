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
import shenanigans.engine.ui.elements.Box
import shenanigans.engine.ui.elements.ColoredBox

fun main() {
    ClientEngine(makeScene(), ClientEngineOptions(SystemList(listOf { UISystem() }))).run()
}

fun makeScene(): Scene {
    val scene = Scene()

    val centered = ColoredBox(listOf(), Color(1f, 0f, 0f))
    val root = Box(listOf(centered))

    root.setJustifyContent(Box.JustifyContent.Center)
    root.setAlignItems(Box.AlignItems.Center)

    centered.setSize(Vector2f(200f, 100f))

    scene.runSystems(
        ResourcesView(), listOf(
            AddEntitiesSystem(
                sequenceOf(
                    sequenceOf(
                        UIComponent(
                            root
                        )
                    )
                )
            )
        )
    )

    return scene
}