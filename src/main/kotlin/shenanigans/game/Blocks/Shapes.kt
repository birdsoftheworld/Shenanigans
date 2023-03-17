package shenanigans.game.Blocks

import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.util.shapes.Polygon
import shenanigans.engine.util.shapes.Rectangle

class Shapes {

    fun getPolygon(block : Component) : Polygon{
        when(block){
            is SpikeBlock -> return spikeShape.polygon
            is TrampolineBlock -> return trampolineShape.polygon
            is OscillatingBlock -> return oscillatingShape.polygon
            is TeleporterBlock -> return teleportShape.polygon
        }
        return Rectangle(0f,0f)
    }
    val spikeShape = Shape(Rectangle(50f,50f),Color(1f,1f,1f))

    val trampolineShape = Shape(Rectangle(50f,50f),Color(1f,1f,1f))

    val stickyShape = Shape(Rectangle(4f,50f), Color(0.56666666666f, 0.60833333333f,0.25555555555f))

    val slipperyShape = Shape(Rectangle(5f,50f), Color(0.38333333333f,.62222222222f,.65833333333f))

    val teleportShape = Shape(Rectangle(25f,25f),Color(1f,1f,1f))

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