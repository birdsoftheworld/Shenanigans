package shenanigans.game.Blocks

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.window.Key
import shenanigans.engine.window.KeyAction
import shenanigans.engine.window.MouseButton
import shenanigans.engine.window.events.KeyboardState
import shenanigans.engine.window.events.MouseState
import java.awt.event.KeyEvent
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
                        Key.NUM_1 -> println("1")
                        Key.NUM_2 -> println("2")
                        Key.NUM_3 -> println("3")
                        Key.NUM_4 -> println("4")
                        Key.NUM_5 -> println("5")
                    }
                }
            }
        }
    }
}

fun insertBlock(block : Component, lifecycle: EntitiesLifecycle){

}
