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

class CollisionSystem : System{

    val radii = hashMapOf<Int, Pair<Float, Int>>()

    override fun query(): Iterable<KClass<out Component>> {
        return listOf(Collider::class, Transform::class)
    }

    override fun execute(entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
//        for (entity in entities) {
//            if(radii.get(entity.id).second == entity.component<Collider>().)
//        }
        val collisionPairs = getCollisionPairs(entities)


    }

    private fun getCollisionPairs (entities: Sequence<EntityView>): MutableList<Pair<EntityView, EntityView>>{
        val collisionPairs = mutableListOf<Pair<EntityView, EntityView>>()

        val prevEntities = mutableListOf<EntityView>()
        entities.forEach { entity ->
            prevEntities.forEach { other ->
                if(Vector2f(entity.component<Transform>().position).sub(other.component<Transform>().position).length() <
                    ((radii[other.id]?.first ?: 0) as Float + (radii[entity.id]?.first ?: 0) as Float)) {
                    collisionPairs.add(Pair(entity, other))
                }
            }
            prevEntities.add(entity)
        }

        return(collisionPairs)
    }

    private fun testCollision(collisionPair: Pair<EntityView, EntityView>){
        val collider1 = collisionPair.first.component<Collider>()
        val collider2 = collisionPair.second.component<Collider>()

//        val normals = setOf<Vector2f>(getNormals(collider1).addAll(getNormals(collider2)))

//        normals.

    }

    private fun getNormals(collider: Collider): MutableList<Vector2f> {
        val normals = mutableListOf<Vector2f>()
        for(i in 0 until (collider.vertices.size - 1)) {
            val side = Vector2f(collider.vertices[i]).sub(collider.vertices[i + 1])
            val normal = Vector2f(-side.y, side.x).normalize()
            if(!normals.contains(normal)) {
                normals.add(normal)
            }
        }
        return(normals)
    }
}