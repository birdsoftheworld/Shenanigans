package shenanigans.game.Blocks

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector3f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.events.eventQueues
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.window.Key
import shenanigans.engine.window.KeyAction
import shenanigans.engine.window.MouseButton
import shenanigans.engine.window.events.KeyboardState
import shenanigans.engine.window.events.MouseState
import shenanigans.game.MousePlayer
import java.awt.event.KeyEvent
import kotlin.math.round
import kotlin.reflect.KClass

class InsertNewEntitiesSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return emptySet()
    }

    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        entities: EntitiesView,
        lifecycle: EntitiesLifecycle
    ) {
        val keyboard = resources.get<KeyboardState>()
        val mouse = resources.get<MouseState>()
        val eventQueue = eventQueues.physics

        if (mouse.isPressed(MouseButton.RIGHT)){
            eventQueues.own.receive(shenanigans.engine.window.events.KeyEvent:: class).forEach { event->
                if(event.action == KeyAction.RELEASE){
                    val pos = resources.get<MouseState>().position()
                    val pos3 = resources.get<CameraResource>().camera!!.untransformPoint(Vector3f(pos.x(),pos.y(),0.5f))
                    when(event.key){
                        Key.NUM_1 -> insertBlock(SpikeBlock(), lifecycle, pos3)
                        Key.NUM_2 -> insertBlock(TrampolineBlock(), lifecycle, pos3)
                        Key.NUM_3 -> insertBlock(OscillatingBlock(), lifecycle, pos3)
                        Key.NUM_4 -> insertBlock((NormalBlock()), lifecycle,  pos3)
                    }
                }
            }
        }
    }
}

fun insertBlock(blockType : Component, lifecycle: EntitiesLifecycle, pos : Vector3f){
    pos.x = round(pos.x / 50) * 50
    pos.y = round(pos.y / 50) * 50
    lifecycle.add(
        sequenceOf(
            Transform(
                pos
            ),
            Sprites().getSprite(blockType),
            Collider(Shapes().getPolygon(blockType), true, false, true),
            MousePlayer(false, Vector2f(0f,0f)),
            blockType
        )
    )
}
