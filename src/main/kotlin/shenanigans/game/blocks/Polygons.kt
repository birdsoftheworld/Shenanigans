package shenanigans.game.blocks

import shenanigans.engine.ecs.Component
import shenanigans.engine.util.shapes.Polygon
import shenanigans.engine.util.shapes.Rectangle

object Polygons {
    fun getPolygon(block : Component) : Polygon {
        when(block){
            is SpikeBlock -> return spikeShape
            is TrampolineBlock -> return trampolineShape
            is OscillatingBlock -> return oscillatingShape
            is TeleporterBlock -> return teleportShape
            is NormalBlock -> return normalShape
        }
        return Rectangle(0f,0f)
    }

    val bigRect = Rectangle(50f,50f)
    val slipperyShape = bigRect
    val stickyShape = bigRect
    val spikeShape = bigRect
    val trampolineShape = bigRect
    val teleportShape = bigRect
    val normalShape = bigRect
    val floorShape = Rectangle(600f, 50f)
    val wallShape = Rectangle(50f, 600f)
    val oscillatingShape = Rectangle(50f, 50f)
}