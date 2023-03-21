package shenanigans.engine.util

import org.joml.Vector2f
import org.joml.Vector2fc
import shenanigans.engine.ecs.EntityView
import shenanigans.engine.physics.Collider
import shenanigans.engine.term.Logger

data class Hit(val distance: Float, val thing: EntityView)

fun raycast(everything: Sequence<EntityView>, point: Vector2fc, direction: Vector2fc, length: Float): Hit? {
    var lowestDist = Float.POSITIVE_INFINITY
    var lowestThing: EntityView? = null
    for (entityView in everything.iterator()) {
        val transform = entityView.component<Transform>().get()
        val collider = entityView.component<Collider>().get()
        val verts = collider.transformedVertices.map { it.add(Vector2f(transform.position.x, transform.position.y), Vector2f()) }
        for(i in verts.indices) {
            val fst = verts[i]
            val snd = verts[(i + 1) % verts.size]
            val value = pointProjectionCollisionDistance(fst to snd, point, direction)
            if (value < lowestDist && value >= 0 && value < length) {
                println(fst)
                println(snd)
                println(point)
                lowestDist = value
                lowestThing = entityView
            }
        }
    }

    Logger.log("raycast", "" + lowestThing?.id)

    return if (lowestThing == null) {
        null
    } else {
        Hit(lowestDist, lowestThing)
    }
}