package shenanigans.game.Blocks

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.util.Transform
import shenanigans.game.MousePlayer
import java.security.cert.TrustAnchor
import kotlin.math.abs
import kotlin.reflect.KClass

enum class direction(sign : Int) {
    Up(0),Right(1),Down(2),Left(3)
}

class OscillatingBlock(val distanceToOscillate : Float, var startPos : Vector2f, var speed : Float, var dir : direction = direction.Right) :
    Component {
    constructor() : this(50f,Vector2f(100f, 500f), .01f )

    fun rotate(clockwise : Boolean){
        if(clockwise){
            when(dir){
                direction.Up -> dir = direction.Right
                direction.Right -> dir = direction.Down
                direction.Down -> dir = direction.Left
                direction.Left -> dir = direction.Up
            }
        }
        else{
            when(dir){
                direction.Down -> dir = direction.Right
                direction.Left -> dir = direction.Down
                direction.Up -> dir = direction.Left
                direction.Right -> dir = direction.Up
            }
        }
        println(dir)
    }

    fun reset(){
        speed = abs(speed)
    }
    fun changeDirection(){
        speed*=-1
    }
    fun newStartPos(x : Float, y : Float){
        this.startPos.set(x,y)
    }
}

class OscillatingBlocksSystem : System {

    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val entities = query(setOf(MousePlayer::class, Transform::class))

        entities.forEach { entity ->
            if(entity.componentOpt<OscillatingBlock>() != null) {
                val pos = entity.component<Transform>().get().position
                val oscillatingBlock = entity.component<OscillatingBlock>().get()
                val mousePlayer = entity.component<MousePlayer>().get()
                if (!mousePlayer.grabbed) {
                    if (abs(pos.x - oscillatingBlock.startPos.x) > oscillatingBlock.distanceToOscillate || abs(pos.y - oscillatingBlock.startPos.y) > oscillatingBlock.distanceToOscillate) {
                        oscillatingBlock.changeDirection()
                    }
                    when (oscillatingBlock.dir) {
                        direction.Up -> {
                            pos.y -= oscillatingBlock.speed
                        }

                        direction.Right -> {
                            pos.x += oscillatingBlock.speed
                        }

                        direction.Down -> {
                            pos.y += oscillatingBlock.speed
                        }

                        direction.Left -> {
                            pos.x -= oscillatingBlock.speed
                        }
                    }
                }
            }
        }
    }
}