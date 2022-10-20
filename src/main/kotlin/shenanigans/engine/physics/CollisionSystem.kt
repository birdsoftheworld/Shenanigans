package shenanigans.engine.physics

import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntitiesLifecycle
import shenanigans.engine.ecs.EntityView
import shenanigans.engine.ecs.System
import shenanigans.engine.ecs.components.Collider
import shenanigans.engine.ecs.components.Transform
import javax.swing.text.html.parser.Entity
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
            val (transform, transformV) = entity.component<Transform>()

            if ((radii[entity.id]?.second ?: -1) < transformV) {
                radii[entity.id] = Pair(Float.NaN, transformV)
            }

            prevEntities.forEach { other ->
                if (Vector2f(transform.position).sub(
                        other.component<Transform>().get().position
                    ).length() < (radii[entity.id]!!.first + radii[other.id]!!.first)
                ) {
                    collisionPairs.add(Pair(entity, other))
                }
            }
            prevEntities.add(entity)
        }

        return (collisionPairs)
    }

    private fun testCollision(collisionPair: Pair<EntityView, EntityView>) {
        val collider1 = collisionPair.first.component<Collider>()
        val collider2 = collisionPair.second.component<Collider>()

//        val normals = setOf<Vector2f>(getNormals(collider1).addAll(getNormals(collider2)))

//        normals.

    }

}

private fun getNormals(collider: Collider): MutableList<Vector2f> {
    val normals = mutableListOf<Vector2f>()

    for (i in 0 until collider.vertices.size) {
        val side = Vector2f(collider.vertices[i]).sub(collider.vertices[i + 1 % collider.vertices.size])
        val normal = Vector2f(-side.y, side.x).normalize()
        if (!normals.contains(normal)) {
            normals.add(normal)
        }
    }

    return normals
}
