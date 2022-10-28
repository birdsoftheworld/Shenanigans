package shenanigans.engine.physics

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.util.Transform
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

class CollisionSystem : System {

    private val radii = hashMapOf<Int, Pair<Float, Int>>()

    override fun query(): Iterable<KClass<out Component>> {
        return listOf(Collider::class, Transform::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {

        val collisionPairs = getCollisionPairs(entities)

        collisionPairs.forEach() { pair ->
            val collision = testCollision(pair)
            val transform1 = pair.first.component<Transform>()
            val transform2 = pair.second.component<Transform>()
            if(!pair.first.component<Collider>().get().static && !pair.second.component<Collider>().get().static) {
                collision.mul(0.5f)
                transform1.get().position.add(collision)
                transform1.mutate()
                transform2.get().position.add(collision.negate())
                transform2.mutate()
            }
            else if(!pair.first.component<Collider>().get().static) {
                transform1.get().position.add(collision)
                transform1.mutate()
            }
            else if(!pair.second.component<Collider>().get().static) {
                transform2.get().position.add(collision.negate())
                transform2.mutate()
            }
        }
    }

    private fun getCollisionPairs(entities: Sequence<EntityView>): MutableList<Pair<EntityView, EntityView>> {
        val collisionPairs = mutableListOf<Pair<EntityView, EntityView>>()

        val prevEntities = mutableListOf<EntityView>()
        entities.forEach { entity ->
            val (transform, transformV) = entity.component<Transform>()

            if ((radii[entity.id]?.second ?: -1) < transformV) {
                var radius = 0f
                entity.component<Collider>().get().vertices.forEach {vertex ->
                    radius = max(radius, vertex.length())
                }
                radii[entity.id] = Pair(radius, transformV)
            }

            prevEntities.forEach { other ->
                if (Vector2f(transform.position).sub(
                        other.component<Transform>().get().position
                    ).length() < (radii[entity.id]!!.first + radii[other.id]!!.first)
                ) {
                    if(!(entity.component<Collider>().get().static && other.component<Collider>().get().static)) {
                        println("test")
                        collisionPairs.add(Pair(entity, other))
                    }
                }
            }
            prevEntities.add(entity)
        }

        return collisionPairs
    }
}
private fun testCollision(collisionPair: Pair<EntityView, EntityView>): Vector2f {
    val collider1 = collisionPair.first.component<Collider>().get()
    val transform1 = collisionPair.first.component<Transform>().get()
    val collider2 = collisionPair.second.component<Collider>().get()
    val transform2 = collisionPair.second.component<Transform>().get()

    val normals = getNormals(collider1, false)
    normals.addAll(getNormals(collider2, true))

    var minCollision = Vector2f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)

    for (normal in normals) {
        val object1Projection = projectionMinMax(collider1, transform1, normal)
        val object2Projection = projectionMinMax(collider2, transform2, normal)

        val overlapDist = min(
            object2Projection.second - object1Projection.first,
            object1Projection.second - object2Projection.first)

        if(overlapDist > 0) {
            normal.mul(overlapDist)
            if(minCollision.length() > normal.length()) minCollision = normal
        }
        else return Vector2f()
    }
    return minCollision
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

private fun getNormals(collider: Collider, negate: Boolean): MutableList<Vector2f> {
    val normals = mutableListOf<Vector2f>()

    for (i in 0 until collider.vertices.size - 1) {
        val side = Vector2f(collider.vertices[i]).sub(collider.vertices[i + 1 % collider.vertices.size])
        val normal = Vector2f(-side.y, side.x).normalize()
        if (!normals.contains(normal)) {
            normals.add(if (negate) normal.negate() else normal)
        }
    }

    return normals
}