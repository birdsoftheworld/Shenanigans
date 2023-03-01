package shenanigans.engine.physics

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector4f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.Event
import shenanigans.engine.events.EventQueue
import shenanigans.engine.util.Transform
import shenanigans.engine.util.setToTransform
import shenanigans.game.ScaryBlock
import shenanigans.game.player.Player
import shenanigans.game.player.PlayerController
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

class CollisionEvent(val normal: Vector2f, val target: EntityId, val with: EntityId) : Event

class CollisionSystem : System {

    private val radii = hashMapOf<EntityId, Pair<Float, Int>>()

    private val transformMatrix = Matrix4f()

    override fun query(): Iterable<KClass<out Component>> {
        return listOf(Collider::class, Transform::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val collisionPairs = getCollisionPairs(entities)

        val eventQueue = resources.get<EventQueue>()
        collisionPairs.forEach { pair ->
            val collision = testCollision(pair) ?: return@forEach

            val transform1 = pair.first.component<Transform>()
            val transform2 = pair.second.component<Transform>()

            val collider1 = pair.first.component<Collider>().get()
            val collider2 = pair.second.component<Collider>().get()

            println(" " + collider1.triggerCollider + " " + collider2.triggerCollider)
            if(!collider1.triggerCollider && !collider2.triggerCollider) {
                println("#2: " + collider1.triggerCollider + " " + collider2.triggerCollider)
                if (!collider1.static && !collider2.static) {
                    val move = Vector2f(collision.normal).mul(collision.scale).mul(0.5f)
                    transform1.get().position.add(move)
                    transform1.mutate()
                    transform2.get().position.add(move.negate())
                    transform2.mutate()
                } else if (!collider1.static) {
                    val move = Vector2f(collision.normal).mul(collision.scale)
                    transform1.get().position.add(move)
                    transform1.mutate()
                } else if (!collider2.static) {
                    val move = Vector2f(collision.normal).mul(collision.scale).negate()
                    transform2.get().position.add(move)
                    transform2.mutate()
                }
                maybeEmitEventsFor(collision.normal, pair.first, pair.second, eventQueue)
                maybeEmitEventsFor(Vector2f(collision.normal).negate(), pair.second, pair.first, eventQueue)
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

            transformMatrix.setToTransform(Vector2f(), transform.rotation, transform.scale)

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
                if (Vector2f(transform.position).sub(
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
    eventQueue: EventQueue
) {
    if (targetEntity.component<Collider>().get().tracked) {
        eventQueue.queueLater(
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
    val transformProj = normal.dot(transform.position)
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