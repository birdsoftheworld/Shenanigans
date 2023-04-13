package shenanigans.game.blocks

import org.joml.Vector2f
import org.joml.Vector3f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.window.Key
import shenanigans.engine.window.KeyAction
import shenanigans.engine.window.MouseButton
import shenanigans.engine.window.events.KeyEvent
import shenanigans.engine.window.events.MouseState
import shenanigans.game.MousePlayer
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
                if(event.action == KeyAction.RELEASE) {
                    val pos = resources.get<MouseState>().position()
                    val pos3 = resources.get<CameraResource>().camera!!.untransformPoint(Vector3f(pos.x(),pos.y(),0.5f))
                    when(event.key) {
                        Key.NUM_1 -> insertBlock(SpikeBlock(), lifecycle, pos3)
                        Key.NUM_2 -> insertBlock(TrampolineBlock(), lifecycle, pos3)
                        Key.NUM_3 -> insertBlock(OscillatingBlock(), lifecycle, pos3)
                        Key.NUM_4 -> insertBlock(NormalBlock(), lifecycle,  pos3)
                    }
                }
            }
        }
    }
}

fun insertBlock(blockType : Component, lifecycle: EntitiesLifecycle, pos : Vector3f){
    pos.x = round((pos.x - 25) / 50) * 50
    pos.y = round((pos.y - 25) / 50) * 50
    if(Sprites.getSprite(blockType) != Sprites.nullSprite) {
        lifecycle.add(
            sequenceOf(
                Transform(
                    pos
                ),
                Sprites.getSprite(blockType),
                Collider(Polygons.getPolygon(blockType), true, tracked = true),
                MousePlayer(false, Vector2f(0f,0f)),
                blockType
            )
        )
    } else {
        lifecycle.add(
            sequenceOf(
                Transform(
                    pos
                ),
                Sprites.getSprite(blockType),
                Collider(Polygons.getPolygon(blockType), true, tracked = true),
                MousePlayer(false, Vector2f(0f,0f)),
                blockType
            )
        )
    }

}
