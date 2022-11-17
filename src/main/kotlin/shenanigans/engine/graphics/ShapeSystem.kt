package shenanigans.engine.graphics

import shenanigans.engine.ecs.*
import shenanigans.engine.graphics.api.RenderSystem
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.graphics.api.ShapeRenderer
import shenanigans.engine.util.Transform
import kotlin.reflect.KClass

/**
 * draws each entity that has a `Shape` and `Transform` component
 */
class ShapeSystem : RenderSystem {
    private val renderer = ShapeRenderer()
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Shape::class, Transform::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val camera = resources.get<CameraResource>().camera
        renderer.projection = camera.getProjectionMatrix()
        renderer.start()
        val view = camera.getViewMatrix()

        for (entity in entities) {
            val shape = entity.component<Shape>().get()
            val transform = entity.component<Transform>().get()
            renderer.transformation = camera.getModelViewMatrix(transform.position, transform.rotation, transform.scale, view)
            renderer.polygon(shape.vertices, shape.color)
        }

        renderer.end()
    }

    override fun discard() {
        renderer.discard()
    }
}
