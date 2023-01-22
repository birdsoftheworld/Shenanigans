package shenanigans.demo.ui

import shenanigans.engine.ClientEngine
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.ecs.utils.AddEntitiesSystem
import shenanigans.engine.init.SystemList
import shenanigans.engine.init.client.ClientEngineOptions
import shenanigans.engine.scene.Scene
import shenanigans.engine.ui.UIRendererComponent
import shenanigans.engine.ui.UISystem
import shenanigans.engine.ui.api.UI

fun main() {
    val ui = run {
        println("Enter the name of the demo you want to run: (counter, layout)")
        print("> ")

        when (readln()) {
            "counter" -> Counter()
            "layout" -> Layout()
            else -> throw IllegalArgumentException()
        }
    }

    ClientEngine(
        makeScene(ui),
        ClientEngineOptions(SystemList(listOf { UISystem() }))
    ).run()
}

fun makeScene(ui: UI): Scene {
    val scene = Scene()

    scene.runSystems(
        ResourcesView(), listOf(
            AddEntitiesSystem(
                sequenceOf(
                    sequenceOf(
                        UIRendererComponent(
                            ui
                        )
                    )
                )
            )
        )
    )

    return scene
}
