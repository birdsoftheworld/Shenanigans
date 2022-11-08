package shenanigans.engine.graphics

import shenanigans.engine.ecs.*
import shenanigans.engine.graphics.api.TextureRenderer
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.resources.CameraResource
import shenanigans.engine.util.Transform
import kotlin.reflect.KClass

class SpriteSystem : System {
    private val renderer = TextureRenderer()

    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Sprite::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val camera = resources.get<CameraResource>().camera
        renderer.projection = camera.getProjectionMatrix()
        renderer.start()
        val view = camera.getViewMatrix()

        for (entity in entities) {
            val sprite = entity.component<Sprite>().get()
            val transform = entity.component<Transform>().get()
            renderer.transformation = camera.getModelViewMatrix(transform.position, transform.rotation, transform.scale, view)
            renderer.textureRect(0f, 0f, sprite.size.x, sprite.size.y, sprite.sprite)
        }

        renderer.end()
    }
}