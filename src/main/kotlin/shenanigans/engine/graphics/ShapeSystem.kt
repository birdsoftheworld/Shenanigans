package shenanigans.engine.graphics

import shenanigans.engine.ecs.*
import shenanigans.engine.graphics.api.ShapeRenderer
import shenanigans.engine.resources.CameraResource
import kotlin.reflect.KClass

class ShapeSystem : System {
    private val renderer = ShapeRenderer()

    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Shape::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val camera = resources.get<CameraResource>().camera
        renderer.camera = camera
        renderer.start()

        for (entity in entities) {
            val shape = entity.component<Shape>().get()
            renderer.polygon(shape.vertices, shape.color)
        }

        renderer.end()
    }
}