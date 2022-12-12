package shenanigans.engine.graphics.api.system

import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntitiesLifecycle
import shenanigans.engine.ecs.EntityView
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.graphics.api.RenderSystem
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.graphics.api.resource.TextureRendererResource
import shenanigans.engine.util.Transform
import kotlin.reflect.KClass

class SpriteSystem : RenderSystem {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Sprite::class)
    }

    override fun execute(resources: ResourcesView, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val renderer = resources.get<TextureRendererResource>().textureRenderer
        val camera = resources.get<CameraResource>().camera
        renderer.projection = camera!!.computeProjectionMatrix()
        renderer.start()
        val view = camera.computeViewMatrix()

        for (entity in entities) {
            val sprite = entity.component<Sprite>().get()
            val transform = entity.component<Transform>().get()
            renderer.transformation = camera.computeModelViewMatrix(transform.position, transform.rotation, transform.scale, view)
            renderer.textureRect(0f, 0f, sprite.size.x, sprite.size.y, sprite.sprite)
        }

        renderer.end()
    }
}
