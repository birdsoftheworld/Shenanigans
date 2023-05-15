package shenanigans.game

import org.joml.Vector2f
import shenanigans.engine.ClientEngine
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.net.Client
import shenanigans.engine.net.Network
import shenanigans.engine.physics.CollisionSystem
import shenanigans.engine.scene.Scene
import shenanigans.engine.ui.UISystem
import shenanigans.engine.util.Transform
import shenanigans.game.control.*
import shenanigans.game.level.block.Block
import shenanigans.game.network.ClientConnectionSystem
import shenanigans.game.network.ClientRegistrationSystem
import shenanigans.game.network.ClientUpdateSystem
import shenanigans.game.network.sendables
import shenanigans.game.player.PlayerController
import shenanigans.game.render.DrawBackgroundSystem
import shenanigans.game.state.ModeChangeSystem
import shenanigans.game.state.ModeManager
import kotlin.reflect.KClass

fun main() {
    val engine = ClientEngine(testScene(), Network(Client(), sendables()))

    Block.initAll()

    engine.runPhysicsOnce(CreatePlayer)

    engine.run()
}

fun testScene(): Scene {
    val scene = Scene()

    scene.defaultSystems.add(ModeChangeSystem())
    scene.defaultSystems.add(MousePlacementSystem())
    scene.defaultSystems.add(PlayerController())
    scene.defaultSystems.add(CollisionSystem())
    scene.defaultSystems.add(UISystem())
    scene.defaultSystems.add(CameraControlSystem())
    scene.defaultSystems.add(ClientUpdateSystem())
    scene.defaultSystems.add(ClientConnectionSystem())
    scene.defaultSystems.add(ClientRegistrationSystem())
    scene.defaultSystems.add(DrawBackgroundSystem())

    return scene
}

object CreatePlayer : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val player = lifecycle.add(
            PlayerController.createPlayer(Transform())
        )

        lifecycle.add(
            sequenceOf(
                CameraManager(FollowingCamera(player, PlayerController::getCameraPosition))
            )
        )

        lifecycle.add(
            sequenceOf(
                ModeManager()
            )
        )

        lifecycle.add(
            sequenceOf(
                PlacementManager()
            )
        )
    }
}