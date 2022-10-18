package shenanigans.engine.physics

import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntitiesLifecycle
import shenanigans.engine.ecs.EntityView
import shenanigans.engine.ecs.System
import shenanigans.engine.ecs.components.Collider
import shenanigans.engine.ecs.components.Transform
import kotlin.reflect.KClass

class CollisionSystem : System{

    val radii = hashMapOf<Int, Pair<Float, Int>>()

    override fun query(): Iterable<KClass<out Component>> {
        return listOf(Collider::class, Transform::class)
    }

    override fun execute(entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        TODO("Not yet implemented")
    }
}