package shenanigans.game.server

import shenanigans.engine.HeadlessEngine
import shenanigans.engine.net.Network
import shenanigans.engine.net.Server
import shenanigans.engine.scene.Scene
import shenanigans.game.level.BuildLevelSystem
import shenanigans.game.level.block.OscillatingBlocksSystem
import shenanigans.game.network.sendables
import shenanigans.game.player.PlayerManagementSystem

fun main() {
    val engine = HeadlessEngine(serverScene(), Network(Server(), sendables()))

    engine.runPhysicsOnce(BuildLevelSystem)

    engine.run()
}

fun serverScene(): Scene {
    return Scene().apply {
        defaultSystems.add(ServerUpdateSystem())
        defaultSystems.add(ServerConnectionSystem())
        defaultSystems.add(ServerRegistrationSystem())

        defaultSystems.add(PlayerManagementSystem())

        defaultSystems.add(OscillatingBlocksSystem())
    }
}