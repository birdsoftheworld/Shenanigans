package shenanigans.engine.graphics.api.system

import shenanigans.engine.ecs.*
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.graphics.api.RenderSystem
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.graphics.api.resource.ShapeRendererResource
import shenanigans.engine.util.Transform
import kotlin.reflect.KClass

/**
 * draws each entity that has a `Shape` and `Transform` component
 */
class ShapeSystem : RenderSystem {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Shape::class, Transform::class)
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val renderer = resources.get<ShapeRendererResource>().shapeRenderer
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
}
