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

    scene.runSystems(
        ResourcesView(), listOf(
            AddEntitiesSystem(
                sequenceOf(
                    sequenceOf(
                        UIComponent(
                            buildUI {
                                box {
                                    minSize = Vector2f(100f, 100f)
                                    color = Color(0.5f, 0.5f, 0.5f)
                                }

                                box {
                                    flexGrow = 1f
                                    flexDirection = Box.FlexDirection.Column
                                    justifyContent = Box.JustifyContent.FlexStart

                                    box {
                                        color = Color(1f, 0f, 0f)
                                        size = Vector2f(200f, 100f)
                                    }

                                    box {
                                        color = Color(0f, 1f, 0f)
                                        flexGrow = 1f
                                    }

                                    box {
                                        color = Color(0f, 0f, 1f)
                                        size = Vector2f(200f, 300f)
                                        alignSelf = Box.Align.FlexEnd
                                    }
                                }
                            }
                        )
                    )
                )
            )
        )
    )

    return scene
}