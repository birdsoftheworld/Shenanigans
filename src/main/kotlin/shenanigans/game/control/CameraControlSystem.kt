package shenanigans.game.control

import org.joml.Vector3f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.window.Key
import shenanigans.engine.window.events.KeyboardState
import kotlin.reflect.KClass

private const val MOVE_SPEED = 300f

class CameraControlSystem : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val first = query(setOf(CameraManager::class)).firstOrNull() ?: return
        val cameraManager = first.component<CameraManager>()
        val mode = cameraManager.get().mode
        if (mode is MovableCamera) {
            val kb = resources.get<KeyboardState>()
            val dt = resources.get<DeltaTime>().deltaTime
            val move = Vector3f()
            if (kb.isPressed(Key.W)) {
                move.y -= MOVE_SPEED
            }
            if (kb.isPressed(Key.S)) {
                move.y += MOVE_SPEED
            }
            if (kb.isPressed(Key.A)) {
                move.x -= MOVE_SPEED
            }
            if (kb.isPressed(Key.D)) {
                move.x += MOVE_SPEED
            }
            mode.pos.add(move.mul(dt.toFloat()))
        }
        val position = when(mode) {
            is StationaryCamera -> mode.pos
            is FollowingCamera -> mode.func(query(emptySet())[mode.target]!!)
            is MovableCamera -> mode.pos
        }
        val camera = resources.get<CameraResource>().camera!!
        val target = Vector3f(position.x() - camera.screenWidth / 2, position.y() - camera.screenHeight / 2, 0f)
        camera.reset().translate(target.x, target.y)
        cameraManager.get().lastPosition = position
        cameraManager.mutate()
    }
}