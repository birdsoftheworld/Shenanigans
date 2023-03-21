package shenanigans.game.Blocks

import jdk.nashorn.internal.ir.Block
import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.util.shapes.Polygon
import shenanigans.engine.util.shapes.Rectangle

class Shapes {
    val smallRect = Rectangle(25f,25f)
    val bigRect = Rectangle(50f,50f)

    fun getPolygon(block : Component) : Polygon{
        when(block){
            is SpikeBlock -> return spikeShape.polygon
            is TrampolineBlock -> return trampolineShape.polygon
            is OscillatingBlock -> return oscillatingShape.polygon
            is TeleporterBlock -> return teleportShape.polygon
            is NormalBlock -> return  normalShape.polygon
        }
        return Rectangle(0f,0f)
    }
    fun getRectangle(block : Component) : Rectangle{
        when(block){
            is SpikeBlock -> return bigRect
            is TrampolineBlock -> return bigRect
            is OscillatingBlock -> return bigRect
            is TeleporterBlock -> return smallRect
            is NormalBlock -> return  bigRect
        }
        return Rectangle(0f,0f)
    }
    val spikeShape = Shape(bigRect,Color(1f,1f,1f))

    val trampolineShape = Shape(bigRect,Color(1f,1f,1f))


    val normalShape = Shape(bigRect,Color(1f,1f,1f))

    val stickyShape = Shape(bigRect, Color(0.56666666666f, 0.60833333333f,0.25555555555f))

    val slipperyShape = Shape(bigRect, Color(0.38333333333f,.62222222222f,.65833333333f))

    val teleportShape = Shape(smallRect,Color(1f,1f,1f))

    val floorShape = Shape(
        Polygon(
            arrayOf(
            Vector2f(0f, 0f),
            Vector2f(0f, 50f),
            Vector2f(600f, 50f),
            Vector2f(600f, 0f))
        ), Color(1f, 0f, 0f)
    )

    val playerShape = Shape(
        Polygon(
            arrayOf(
            Vector2f(0f, 0f), Vector2f(0f, 30f), Vector2f(30f, 30f), Vector2f(30f, 0f))
        ), Color(0f, 0f, 1f)
    )

    val wallShape = Shape(
        Polygon(
            arrayOf(
            Vector2f(0f, 0f),
            Vector2f(0f, 600f),
            Vector2f(50f, 600f),
            Vector2f(50f, 0f))
        ), Color(0f, 1f, 1f)
    )

    val oscillatingShape = Shape(
        Polygon(
        arrayOf(
            Vector2f(0f, 0f),
            Vector2f(0f, 50f),
            Vector2f(50f, 50f),
            Vector2f(50f, 0f))
        ), Color(.5f, .5f, .5f)
    )
}