package shenanigans.engine.graphics.api.system

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.graphics.api.resource.ShapeRendererResource
import shenanigans.engine.util.Transform
import shenanigans.engine.util.camera.CameraResource
import kotlin.reflect.KClass

/**
 * draws each entity that has a `Shape` and `Transform` component
 */
class ShapeSystem : System {
    override fun executeRender(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val renderer = resources.get<ShapeRendererResource>().shapeRenderer
        val camera = resources.get<CameraResource>().camera
        renderer.projection = camera!!.computeProjectionMatrix()
        renderer.start()
        val view = camera.computeViewMatrix()

        for (entity in query(setOf(Shape::class, Transform::class))) {
            val shape = entity.component<Shape>().get()
            val transform = entity.component<Transform>().get()
            renderer.transformation =
                camera.computeModelViewMatrix(transform.position, transform.rotation, transform.scale, view)
            renderer.polygon(shape.polygon.vertices, shape.color)
        }

        renderer.end()
    }
}
