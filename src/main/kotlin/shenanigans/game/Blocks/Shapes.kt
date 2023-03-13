package shenanigans.game.Blocks

import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.game.newShape

class Shapes {

    fun getShape(block : Component): Shape{
        when(block){
            is SpikeBlock -> return spikeShape
            is TrampolineBlock -> return trampolineShape
            is OscillatingBlock -> return oscillatingShape
            is TeleporterBlock -> return teleportShape
        }
        return newShape(10000f,10000f)
    }
    val spikeShape = newShape(50f,50f)

    val trampolineShape = newShape(50f,50f)

    val stickyShape = newShape(4f,50f, Color(0.56666666666f, 0.60833333333f,0.25555555555f))

    val slipperyShape = newShape(5f,50f, Color(0.38333333333f,.62222222222f,.65833333333f))

    val teleportShape = newShape(25f,25f)

    val floorShape = Shape(
        arrayOf(
            Vector2f(0f, 0f),
            Vector2f(0f, 50f),
            Vector2f(600f, 50f),
            Vector2f(600f, 0f)
        ), Color(1f, 0f, 0f)
    )

    val playerShape = Shape(
        arrayOf(
            Vector2f(0f, 0f), Vector2f(0f, 30f), Vector2f(30f, 30f), Vector2f(30f, 0f)
        ), Color(0f, 0f, 1f)
    )

    val wallShape = Shape(
        arrayOf(
            Vector2f(0f, 0f),
            Vector2f(0f, 600f),
            Vector2f(50f, 600f),
            Vector2f(50f, 0f)
        ), Color(0f, 1f, 1f)
    )

    val oscillatingShape = Shape(
        arrayOf(
            Vector2f(0f, 0f),
            Vector2f(0f, 50f),
            Vector2f(50f, 50f),
            Vector2f(50f, 0f)
        ), Color(.5f, .5f, .5f)
    )
}