package shenanigans.game

import shenanigans.engine.ClientEngine
import shenanigans.engine.net.Client
import shenanigans.engine.net.Network
import shenanigans.engine.physics.CollisionSystem
import shenanigans.engine.scene.Scene
import shenanigans.game.control.CameraControlSystem
import shenanigans.game.control.MouseMovementSystem
import shenanigans.game.level.BuildLevelSystem
import shenanigans.game.level.InsertNewEntitiesSystem
import shenanigans.game.level.block.OscillatingBlocksSystem
import shenanigans.game.network.ClientConnectionSystem
import shenanigans.game.network.ClientRegistrationSystem
import shenanigans.game.network.ClientUpdateSystem
import shenanigans.game.network.sendables
import shenanigans.game.player.PlayerController
import shenanigans.game.render.DrawBackgroundSystem
import shenanigans.game.state.ModeChangeSystem

fun main() {
    val engine = ClientEngine(testScene(), Network(Client(), sendables()))

    engine.runPhysicsOnce(BuildLevelSystem())

    engine.run()
}

fun testScene(): Scene {
    val scene = Scene()

    scene.defaultSystems.add(InsertNewEntitiesSystem())
    scene.defaultSystems.add(OscillatingBlocksSystem())
    scene.defaultSystems.add(ModeChangeSystem())
    scene.defaultSystems.add(MouseMovementSystem())
    scene.defaultSystems.add(PlayerController())
    scene.defaultSystems.add(CollisionSystem())
    scene.defaultSystems.add(CameraControlSystem())
    scene.defaultSystems.add(ClientUpdateSystem())
    scene.defaultSystems.add(ClientConnectionSystem())
    scene.defaultSystems.add(ClientRegistrationSystem())
    scene.defaultSystems.add(DrawBackgroundSystem())

    return scene
}
