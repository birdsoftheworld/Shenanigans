package shenanigans.game.network

import shenanigans.engine.HeadlessEngine
import shenanigans.engine.net.Network
import shenanigans.engine.net.SendableClass
import shenanigans.engine.net.Server
import shenanigans.engine.network.FullEntitySyncSystem
import shenanigans.engine.network.ServerRegistrationSystem
import shenanigans.engine.network.ServerUpdateSystem
import shenanigans.engine.scene.Scene

fun main() {
    HeadlessEngine(testServerScene(), Network(Server(), sendables())).run()
}

fun testServerScene(): Scene {
    val scene = Scene()

    scene.defaultSystems.add(ServerUpdateSystem())
    scene.defaultSystems.add(ServerRegistrationSystem())
    scene.defaultSystems.add(FullEntitySyncSystem())

    return scene
}