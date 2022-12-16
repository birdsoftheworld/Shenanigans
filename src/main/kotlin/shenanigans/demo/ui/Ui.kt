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

    val child1 = ColoredBox(listOf(), Color(1f, 0f, 0f))
    val child2 = ColoredBox(listOf(), Color(0f, 1f, 0f))
    val child3 = ColoredBox(listOf(), Color(0f, 0f, 1f))
    val root = Box(listOf(child1, child2, child3))

    root.setFlexDirection(Box.FlexDirection.Column)
    root.setJustifyContent(Box.JustifyContent.FlexStart)

    child1.setSize(Vector2f(200f, 100f))

    child2.setGrow()

    child2.setFlexDirection(Box.FlexDirection.Row)
    child2.setFlexWrap(Box.FlexWrap.Wrap)

    child3.setSize(Vector2f(200f, 100f))
    child3.setAlignSelf(Box.Align.FlexEnd)

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