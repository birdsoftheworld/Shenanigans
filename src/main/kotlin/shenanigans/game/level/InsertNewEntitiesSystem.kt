package shenanigans.game.level

import org.joml.Vector2f
import org.joml.Vector3f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.window.Key
import shenanigans.engine.window.KeyAction
import shenanigans.engine.window.MouseButton
import shenanigans.engine.window.events.KeyEvent
import shenanigans.engine.window.events.MouseState
import shenanigans.game.control.MouseDraggable
import shenanigans.game.level.block.*
import shenanigans.game.network.Synchronized
import kotlin.math.round
import kotlin.reflect.KClass

class InsertNewEntitiesSystem : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val mouse = resources.get<MouseState>()

        if (mouse.isPressed(MouseButton.RIGHT)) {
            eventQueues.own.receive(KeyEvent::class).forEach { event ->
                if (event.action == KeyAction.RELEASE) {
                    val pos =
                        resources.get<CameraResource>().camera!!.untransformPoint(
                            Vector3f(
                                resources.get<MouseState>().position(), 0.5f
                            )
                        )
                    when (event.key) {
                        Key.NUM_1 -> insertBlock(lifecycle, SpikeBlock(), pos)
                        Key.NUM_2 -> insertBlock(lifecycle, TrampolineBlock(), pos)
                        Key.NUM_3 -> insertBlock(lifecycle, OscillatingBlock(), pos)
                        Key.NUM_4 -> insertBlock(lifecycle, NormalBlock(SQUARE_BLOCK_SHAPE), pos)
                    }
                }
            }
        }
    }
}

fun insertBlock(lifecycle: EntitiesLifecycle, block: Block, pos: Vector3f) {
    pos.x = round((pos.x - 32) / 64) * 64
    pos.y = round((pos.y - 32) / 64) * 64

    val components = block.toComponents(pos).plus(
        sequenceOf(
            Synchronized(),
            MouseDraggable(false, Vector2f(0f, 0f)),
        )
    )

    lifecycle.add(components)
}
