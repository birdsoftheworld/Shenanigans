package shenanigans.engine.graphics.api.system

import org.joml.Vector2i
import org.joml.Vector3f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.TextureOptions
import shenanigans.engine.graphics.api.resource.TextureRendererResource
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.window.WindowResource
import kotlin.reflect.KClass

class DrawBackgroundSystem : System {
    private val background =
        TextureManager.createTexture(TextureKey("background"), "/background.png", TextureOptions(wrapping = TextureOptions.WrappingType.REPEAT))
    private val imageSize = Vector2i(400, 400)

    override fun query(): Iterable<KClass<out Component>> {
        return emptySet()
    }

    override fun executeRender(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        entities: EntitiesView,
        lifecycle: EntitiesLifecycle
    ) {
        val textureRenderer = resources.get<TextureRendererResource>().textureRenderer
        val size = resources.get<WindowResource>().window.size
        val camera = resources.get<CameraResource>().camera!!
        val translation = camera.untransformPoint(Vector3f())

        val view = camera.computeViewMatrix()
        textureRenderer.projection = camera.computeProjectionMatrix()
        textureRenderer.start()

        textureRenderer.transformation = view
        textureRenderer.textureRect(translation.x, translation.y, -1f, size.x.toFloat(), size.y.toFloat(), background.getRegion(
            translation.x / imageSize.x, translation.y / imageSize.y, size.x.toFloat() / imageSize.x, size.y.toFloat() / imageSize.y
        ))
        textureRenderer.textureRect(
            translation.x, translation.y, size.x.toFloat(), size.y.toFloat(), background.getRegion(
                translation.x / imageSize.x,
                translation.y / imageSize.y,
                size.x.toFloat() / imageSize.x,
                size.y.toFloat() / imageSize.y
            )
        )

        textureRenderer.end()
    }
}