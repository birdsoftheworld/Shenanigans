package shenanigans.engine.graphics.api.system

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.graphics.api.resource.TextureRendererResource
import shenanigans.engine.util.Transform
import kotlin.reflect.KClass

class SpriteSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(Sprite::class)
    }

    override fun executeRender(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        entities: EntitiesView,
        lifecycle: EntitiesLifecycle
    ) {
        val renderer = resources.get<TextureRendererResource>().textureRenderer
        val camera = resources.get<CameraResource>().camera
        renderer.projection = camera!!.computeProjectionMatrix()
        renderer.start()
        val view = camera.computeViewMatrix()

        for (entity in entities) {
            val sprite = entity.component<Sprite>().get()
            val transform = entity.component<Transform>().get()
            renderer.transformation = camera.computeModelViewMatrix(transform.position, transform.rotation, transform.scale, view)
            renderer.textureRect(0f, 0f, sprite.rectangle.width, sprite.rectangle.height, sprite.sprite)
        }

        renderer.end()
    }
}
