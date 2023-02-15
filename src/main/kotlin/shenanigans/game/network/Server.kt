package shenanigans.game.network

import shenanigans.engine.HeadlessEngine
import shenanigans.engine.network.ServerUpdateSystem
import shenanigans.engine.scene.Scene

fun main() {
    HeadlessEngine(testServerScene()).run()
}

fun testServerScene(): Scene {
    val scene = Scene()

    scene.defaultSystems.add(ServerUpdateSystem())

    return scene
}