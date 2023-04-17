package shenanigans.game.network

import shenanigans.engine.HeadlessEngine
import shenanigans.engine.net.Network
import shenanigans.engine.net.Server
import shenanigans.engine.network.ServerConnectionSystem
import shenanigans.engine.network.ServerRegistrationSystem
import shenanigans.engine.network.ServerUpdateSystem
import shenanigans.engine.scene.Scene

fun main() {
    HeadlessEngine(testServerScene(), Network(Server(), sendables())).run()
}

fun testServerScene(): Scene {
    val scene = Scene()

    scene.defaultSystems.add(ServerUpdateSystem())
    scene.defaultSystems.add(ServerConnectionSystem())
    scene.defaultSystems.add(ServerRegistrationSystem())

    return scene
}