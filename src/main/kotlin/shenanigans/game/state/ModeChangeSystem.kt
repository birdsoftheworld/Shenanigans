package shenanigans.game.state

import org.joml.Vector3f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.window.Key
import shenanigans.engine.window.events.KeyboardState
import shenanigans.game.control.CameraManager
import shenanigans.game.control.FollowingCamera
import shenanigans.game.control.MovableCamera
import shenanigans.game.player.Player
import shenanigans.game.player.PlayerController
import kotlin.reflect.KClass

class ModeChangeSystem : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val mmComponent = query(setOf(ModeManager::class)).firstOrNull()?.component<ModeManager>() ?: return
        val modeManager = mmComponent.get()
        val cmComponent = query(setOf(CameraManager::class)).firstOrNull()?.component<CameraManager>() ?: return
        val cameraManager = cmComponent.get()

        val keyboard = resources.get<KeyboardState>()
        if (keyboard.isJustPressed(Key.B)) {
            if (modeManager.mode == Mode.PLAY) {
                for (entityView in query(setOf(Player::class))) {
                    lifecycle.del(entityView.id)
                }
                cameraManager.mode = MovableCamera(Vector3f(cameraManager.lastPosition))
                modeManager.mode = Mode.PLACE
            } else {
                val id = lifecycle.add(PlayerController.createPlayer(cameraManager.lastPosition))
                cameraManager.mode = FollowingCamera(id, PlayerController::getCameraPosition)
                modeManager.mode = Mode.PLAY
            }
            mmComponent.mutate()
        }
    }
}