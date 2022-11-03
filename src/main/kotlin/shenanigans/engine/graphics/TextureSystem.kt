package shenanigans.engine.graphics

import shenanigans.engine.ecs.*
import shenanigans.engine.graphics.api.ShapeRenderer
import shenanigans.engine.graphics.api.TextureRenderer
import shenanigans.engine.resources.CameraResource
import kotlin.reflect.KClass

class TextureSystem : System {
    private val renderer = TextureRenderer()

    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Shape::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        renderer.camera = resources.get<CameraResource>().camera
        renderer.start()

        for (entity in entities) {
            val shape = entity.component<Shape>().get()
            renderer.textureRect(shape.vertices[0].x, shape.vertices[0].y, 200f, 200f, shape.texture)
        }

        renderer.end()
    }
}