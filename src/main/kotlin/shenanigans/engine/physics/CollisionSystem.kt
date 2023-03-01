package shenanigans.engine.physics

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.Event
import shenanigans.engine.events.EventQueues
import shenanigans.engine.util.Transform
import shenanigans.engine.util.dot
import shenanigans.engine.util.setToTransform
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

class CollisionEvent(val normal: Vector2f, val target: UUID, val with: UUID) : Event

class CollisionSystem : System {

    private val radii = hashMapOf<UUID, Pair<Float, Int>>()

    private val transformMatrix = Matrix4f()

    override fun query(): Iterable<KClass<out Component>> {
        return listOf(Collider::class, Transform::class)
    }

    override fun executePhysics(resources: ResourcesView, eventQueues: EventQueues, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val collisionPairs = getCollisionPairs(entities)

        collisionPairs.forEach { pair ->
            val collision = testCollision(pair) ?: return@forEach

            val transform1 = pair.first.component<Transform>()
            val transform2 = pair.second.component<Transform>()

            val collider1 = pair.first.component<Collider>().get()
            val collider2 = pair.second.component<Collider>().get()
            if(!collider1.triggerCollider && !collider2.triggerCollider) {
                if (!collider1.static && !collider2.static) {
                    val move = Vector3f(collision.normal, 0f).mul(collision.scale).mul(0.5f)
                    transform1.get().position.add(move)
                    transform1.mutate()
                    transform2.get().position.add(move.negate())
                    transform2.mutate()
                } else if (!collider1.static) {
                    val move = Vector3f(collision.normal, 0f).mul(collision.scale)
                    transform1.get().position.add(move)
                    transform1.mutate()
                } else if (!collider2.static) {
                    val move = Vector3f(collision.normal, 0f).mul(collision.scale).negate()
                    transform2.get().position.add(move)
                    transform2.mutate()
                }

                maybeEmitEventsFor(collision.normal, pair.first, pair.second, eventQueues)
                maybeEmitEventsFor(Vector2f(collision.normal).negate(), pair.second, pair.first, eventQueues)
            }
        }
        return
    }

    private fun getCollisionPairs(entities: Sequence<EntityView>): MutableList<Pair<EntityView, EntityView>> {
        val collisionPairs = mutableListOf<Pair<EntityView, EntityView>>()

        val prevEntities = mutableListOf<EntityView>()
        entities.forEach { entity ->
            val (transform, transformV) = entity.component<Transform>()

            val collider = entity.component<Collider>().get()

            transformMatrix.setToTransform(Vector3f(), transform.rotation, transform.scale)

            for (i in 0 until collider.vertices.size) {
                val vertex = Vector4f(collider.vertices[i], 0f, 1f).mul(transformMatrix)
                collider.transformedVertices[i].x = vertex.x
                collider.transformedVertices[i].y = vertex.y
            }

            if ((radii[entity.id]?.second ?: -1) < transformV) {
                var radius = 0f
                collider.transformedVertices.forEach { vertex ->
                    radius = max(radius, vertex.length())
                }
                radii[entity.id] = Pair(radius, transformV)
            }

            prevEntities.forEach { other ->
                if (Vector3f(transform.position).sub(
                        other.component<Transform>().get().position
                    ).length() < (radii[entity.id]!!.first + radii[other.id]!!.first)
                ) {
                    if (!(entity.component<Collider>().get().static && other.component<Collider>().get().static)) {
                        collisionPairs.add(Pair(entity, other))
                    }
                }
            }
            prevEntities.add(entity)
        }

        return collisionPairs
    }
}

private fun maybeEmitEventsFor(
    normal: Vector2f,
    targetEntity: EntityView,
    with: EntityView,
    eventQueues: EventQueues
) {
    if (targetEntity.component<Collider>().get().tracked) {
        eventQueues.own.queueLater(
            CollisionEvent(
                normal, targetEntity.id, with.id
            )
        )
    }
}

private class Collision(val normal: Vector2f, val scale: Float)

private fun testCollision(collisionPair: Pair<EntityView, EntityView>): Collision? {
    val collider1 = collisionPair.first.component<Collider>().get()
    val transform1 = collisionPair.first.component<Transform>().get()
    val collider2 = collisionPair.second.component<Collider>().get()
    val transform2 = collisionPair.second.component<Transform>().get()

    val normals = getNormals(collider1)
    normals.addAll(getNormals(collider2))

    var minNormal = Vector2f(0f, 0f)
    var minOverlap = Float.POSITIVE_INFINITY

    for (normal in normals) {
        val object1Projection = projectionMinMax(collider1, transform1, normal)
        val object2Projection = projectionMinMax(collider2, transform2, normal)

        val overlapDist = min(
            object2Projection.second - object1Projection.first,
            object1Projection.second - object2Projection.first
        )

        if (object1Projection.first < object2Projection.first) normal.negate()
        if (overlapDist < 0) {
            return null
        }
        if (overlapDist < minOverlap) {
            minOverlap = overlapDist
            minNormal = normal
        }
    }

    return Collision(minNormal, minOverlap)
}

private fun projectionMinMax(collider: Collider, transform: Transform, normal: Vector2f): Pair<Float, Float> {
    var projectionMin = Float.POSITIVE_INFINITY
    var projectionMax = Float.NEGATIVE_INFINITY
    val transformProj = normal.dot(transform.position.x, transform.position.y)
    for (vertex in collider.transformedVertices) {
        val proj = vertex.dot(normal)
        projectionMin = min(projectionMin, proj)
        projectionMax = max(projectionMax, proj)
    }
    return Pair(projectionMin + transformProj, projectionMax + transformProj)
}

private fun getNormals(collider: Collider): MutableSet<Vector2f> {
    val normals = mutableSetOf<Vector2f>()

    for (i in 0 until collider.transformedVertices.size) {
        val side =
            Vector2f(collider.transformedVertices[i]).sub(collider.transformedVertices[(i + 1) % (collider.transformedVertices.size)])
        val normal = Vector2f(-side.y, side.x).normalize()
        normals.add(normal)
    }

    return normals
}