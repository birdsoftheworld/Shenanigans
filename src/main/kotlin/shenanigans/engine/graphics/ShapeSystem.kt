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
        renderer.camera = resources.get<CameraResource>().camera
        renderer.start()

        for (entity in entities) {
            val shape = entity.component<Shape>().get()
            renderer.rect(shape.vertices[0].x, shape.vertices[0].y, 50f, 50f, shape.color)
        }

        renderer.end()
    }
}