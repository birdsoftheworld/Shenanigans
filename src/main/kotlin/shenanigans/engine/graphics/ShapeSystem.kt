package shenanigans.engine.graphics

import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntitiesLifecycle
import shenanigans.engine.ecs.EntityView
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.graphics.api.CameraResource
import shenanigans.engine.graphics.api.RenderSystem
import shenanigans.engine.graphics.api.ShapeRenderer
import shenanigans.engine.graphics.api.TextureRenderer
import shenanigans.engine.graphics.api.component.Shape
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

    override fun execute(resources: ResourcesView, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val camera = resources.get<CameraResource>().camera
        renderer.projection = camera!!.computeProjectionMatrix()
        renderer.start()
        val view = camera.computeViewMatrix()

        for (entity in entities) {
            val shape = entity.component<Shape>().get()
            val transform = entity.component<Transform>().get()
            renderer.transformation = camera.computeModelViewMatrix(transform.position, transform.rotation, transform.scale, view)
            renderer.polygon(shape.vertices, shape.color)
        }

        renderer.end()
    }

    override fun discard() {
        renderer.discard()
    }
}
