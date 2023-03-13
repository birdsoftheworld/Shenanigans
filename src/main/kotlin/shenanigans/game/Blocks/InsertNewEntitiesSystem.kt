package shenanigans.game.Blocks

import org.joml.Vector2f
import org.joml.Vector2fc
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
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

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val keyboard = resources.get<KeyboardState>()
        val mouse = resources.get<MouseState>()
        val eventQueue = resources.get<EventQueue>()

        if (mouse.isPressed(MouseButton.RIGHT)){
            eventQueue.iterate<shenanigans.engine.window.events.KeyEvent>().forEach { event->
                if(event.action == KeyAction.RELEASE){
                    when(event.key){
                        Key.NUM_1 -> insertBlock(SpikeBlock(), lifecycle, resources.get<CameraResource>().camera!!.untransformPoint(Vector2f(resources.get<MouseState>().position())))
                        Key.NUM_2 -> insertBlock(TrampolineBlock(), lifecycle, resources.get<CameraResource>().camera!!.untransformPoint(Vector2f(resources.get<MouseState>().position())))
                        Key.NUM_3 -> insertBlock(OscillatingBlock(), lifecycle, resources.get<CameraResource>().camera!!.untransformPoint(Vector2f(resources.get<MouseState>().position())))
                        Key.NUM_4 -> insertBlock((NormalBlock()), lifecycle, resources.get<CameraResource>().camera!!.untransformPoint(Vector2f(resources.get<MouseState>().position())))
                    }
                }
            }
        }
    }
}

fun insertBlock(blockType : Component, lifecycle: EntitiesLifecycle, pos : Vector2f){
    pos.x = round(pos.x / 50) * 50
    pos.y = round(pos.y / 50) * 50
    lifecycle.add(
        sequenceOf(
            Transform(
                pos
            ),
            Sprites().getSprite(blockType),
            Collider(Shapes().getShape(blockType), true, false, true),
            MousePlayer(false, Vector2f(0f,0f)),
            blockType
        )
    )
}
