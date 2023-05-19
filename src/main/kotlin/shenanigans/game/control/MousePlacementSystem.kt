package shenanigans.game.control

import org.joml.Vector2f
import org.joml.Vector3f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.net.ClientOnly
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.util.isPointInside
import shenanigans.engine.window.Key
import shenanigans.engine.window.KeyAction
import shenanigans.engine.window.MouseButton
import shenanigans.engine.window.MouseButtonAction
import shenanigans.engine.window.events.KeyEvent
import shenanigans.engine.window.events.MouseButtonEvent
import shenanigans.engine.window.events.MouseState
import shenanigans.game.level.block.*
import shenanigans.game.level.insertBlock
import shenanigans.game.level.roundBlockPosition
import shenanigans.game.state.Mode
import shenanigans.game.state.ModeChangeEvent
import shenanigans.game.state.ModeManager
import kotlin.reflect.KClass

@ClientOnly
object HeldObject : Component

class Placeable(val sprite: Sprite, val factory: () -> Block)

@ClientOnly
class PlacementManager : Component {
    var heldPlaceable: Placeable? = null
}

class MousePlacementSystem : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val modeManager = query(setOf(ModeManager::class)).first().component<ModeManager>().get()
        val placementManager = query(setOf(PlacementManager::class)).first().component<PlacementManager>()

        eventQueues.own.receive(ModeChangeEvent::class).forEach { _ ->
            query(setOf(HeldObject::class)).forEach {
                lifecycle.del(it.id)
            }

            placementManager.get().heldPlaceable = null
            placementManager.mutate()
        }

        if (modeManager.mode != Mode.BUILD) return

        val position = resources.get<MouseState>().position()
        val transformedPosition = resources.get<CameraResource>().camera!!.untransformPoint(
            Vector3f(
                position,
                0f
            )
        )
        val roundedPosition = roundBlockPosition(Vector3f(transformedPosition))

        eventQueues.own.receive(KeyEvent::class).forEach { event ->
            if (event.action == KeyAction.PRESS) {
                val placeable = when (event.key) {
                    Key.NUM_1 -> Placeable(NormalBlock().createSprite()) { NormalBlock() }
                    Key.NUM_2 -> Placeable(TrampolineBlock().createSprite()) { TrampolineBlock() }
                    Key.NUM_3 -> Placeable(SpikeBlock().createSprite()) { SpikeBlock() }
                    Key.NUM_4 -> Placeable(StickyBlock().createSprite()) { StickyBlock() }
                    Key.NUM_5 -> Placeable(IceBlock().createSprite()) { IceBlock() }
                    Key.NUM_6 -> Placeable(OscillatingBlock().createSprite()) { OscillatingBlock() }
                    Key.NUM_7 -> Placeable(CrumbleBlock().createSprite()) { CrumbleBlock() }
//                    Key.NUM_8 -> Placeable(GoalBlock().createSprite()) { GoalBlock() }
//                    Key.NUM_9 -> Placeable(RespawnBlock().createSprite()) { RespawnBlock() }
                    Key.NUM_0 -> Placeable(AccelerationBlock().createSprite()) { AccelerationBlock() }
                    Key.ESCAPE -> {
                        query(setOf(HeldObject::class)).forEach {
                            lifecycle.del(it.id)
                        }
                        null
                    }

                    else -> null
                }

                if (placeable != null) {
                    query(setOf(HeldObject::class)).forEach {
                        lifecycle.del(it.id)
                    }

                    placementManager.get().heldPlaceable = placeable
                    placementManager.mutate()

                    lifecycle.add(sequenceOf(HeldObject, placeable.sprite, Transform(roundedPosition)))
                    return
                }
            }
        }

        val placeable = placementManager.get().heldPlaceable
        if (placeable != null) {
            query(setOf(HeldObject::class, Transform::class)).iterator().forEach {
                val component = it.component<Transform>()
                component.get().position = roundedPosition
                component.mutate()
            }

            eventQueues.own.receive(MouseButtonEvent::class).forEach { event ->
                query(setOf(HeldObject::class)).forEach {
                    lifecycle.del(it.id)
                }
                when (event.button) {
                    MouseButton.BUTTON_1 -> {
                        insertBlock(lifecycle, placeable.factory(), roundedPosition, modifiable = true)
                    }
                }
                placementManager.get().heldPlaceable = null
                placementManager.mutate()
            }
        } else {
            eventQueues.own.receive(MouseButtonEvent::class).forEach { event ->
                when (event.button) {
                    MouseButton.BUTTON_2 -> {
                        if (event.action == MouseButtonAction.PRESS) {
                            query(setOf(Modifiable::class, Collider::class, Transform::class)).iterator().forEach {
                                val transform = it.component<Transform>().get()
                                val collider = it.component<Collider>().get()
                                if (collider.polygon.isPointInside(
                                        Vector2f(
                                            transformedPosition.x,
                                            transformedPosition.y
                                        ), transform
                                    )
                                ) {
                                    lifecycle.del(it.id)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}