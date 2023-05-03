package shenanigans.game.server

import shenanigans.engine.HeadlessEngine
import shenanigans.engine.net.Network
import shenanigans.engine.net.Server
import shenanigans.engine.scene.Scene
import shenanigans.game.network.sendables

fun main() {
    HeadlessEngine(serverScene(), Network(Server(), sendables())).run()
}

fun serverScene(): Scene {
    return Scene().apply {
        defaultSystems.add(ServerUpdateSystem())
        defaultSystems.add(ServerConnectionSystem())
        defaultSystems.add(ServerRegistrationSystem())
    }
}