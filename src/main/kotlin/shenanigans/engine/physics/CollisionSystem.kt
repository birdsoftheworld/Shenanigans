package shenanigans.engine.physics

import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntitiesLifecycle
import shenanigans.engine.ecs.EntityView
import shenanigans.engine.ecs.System
import shenanigans.engine.ecs.components.Collider
import shenanigans.engine.ecs.components.Transform
import java.util.Vector
import javax.swing.text.html.parser.Entity
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

class CollisionSystem : System {

    private val radii = hashMapOf<Int, Pair<Float, Int>>()

    override fun query(): Iterable<KClass<out Component>> {
        return listOf(Collider::class, Transform::class)
    }

    override fun execute(entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
//        for (entity in entities) {
//            if(radii.get(entity.id).second == entity.component<Collider>().)
//        }
        val collisionPairs = getCollisionPairs(entities)
    }

    private fun getCollisionPairs(entities: Sequence<EntityView>): MutableList<Pair<EntityView, EntityView>> {
        val collisionPairs = mutableListOf<Pair<EntityView, EntityView>>()

        val prevEntities = mutableListOf<EntityView>()
        entities.forEach { entity ->
            val (transform, transformV) = entity.component<Transform>();

            if ((radii[entity.id]?.second ?: -1) < transformV) {
                radii[entity.id] = Pair(Float.NaN, transformV)
            }

            prevEntities.forEach { other ->
                if (Vector2f(transform.position).sub(
                        other.component<Transform>().get().position
                    ).length() < ((radii[other.id]?.first ?: 0) as Float + (radii[entity.id]?.first ?: 0) as Float)
                ) {
                    collisionPairs.add(Pair(entity, other))
                }
            }
            prevEntities.add(entity)
        }

        return collisionPairs
    }
}
private fun testCollision(collisionPair: Pair<EntityView, EntityView>) {
    val collider1 = collisionPair.first.component<Collider>().get()
    val transform1 = collisionPair.first.component<Transform>().get()
    val collider2 = collisionPair.second.component<Collider>().get()
    val transform2 = collisionPair.second.component<Transform>().get()

    val normals = getNormals(collider1)
    normals.addAll(getNormals(collider2))

    val maxCollision = Vector2f()

    for (normal in normals) {
        var object1Projection = projectionMinMax(collider1, transform1, normal)
        val object2Projection = projectionMinMax(collider2, transform2, normal)
    }
}

private fun projectionMinMax(collider : Collider, transform : Transform, normal: Vector2f) : Pair<Float, Float> {
    var projectionMin = Float.POSITIVE_INFINITY
    var projectionMax = Float.NEGATIVE_INFINITY
    val transformProj = normal.dot(transform.position)
    for (vertex in collider.vertices) {
        val proj = Vector2f(vertex).add(transform.position).dot(normal)
        projectionMin = min(projectionMin, proj)
        projectionMax = max(projectionMax, proj)
    }
    return Pair(projectionMin + transformProj, projectionMax + transformProj)
}

private fun getNormals(collider: Collider): MutableList<Vector2f> {
    val normals = mutableListOf<Vector2f>()

    for (i in 0 until (collider.vertices.size - 1)) {
        val side = Vector2f(collider.vertices[i]).sub(collider.vertices[i + 1])
        val normal = Vector2f(-side.y, side.x).normalize()
        if (!normals.contains(normal)) {
            normals.add(normal)
        }
    }

    return normals
}