package shenanigans.engine.graphics

import org.joml.Vector2i
import shenanigans.engine.ecs.*
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.FontRenderer
import shenanigans.engine.graphics.api.RenderSystem
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.graphics.api.ShapeRenderer
import shenanigans.engine.graphics.api.font.Font
import shenanigans.engine.util.Transform
import kotlin.reflect.KClass

/**
 * draws each entity that has a `Shape` and `Transform` component
 */
class ShapeSystem : RenderSystem {
    private val renderer = ShapeRenderer()
    private val fontRenderer = FontRenderer()
    private val font = Font.fromFile("/NotoSans-Medium.ttf")
    private val bitmapFont = font.createSized(50f)
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

        fontRenderer.start()
        fontRenderer.projection = camera.getProjectionMatrix()
        for (entity in entities) {
            val shape = entity.component<Shape>().get()
            val transform = entity.component<Transform>().get()
            fontRenderer.transformation = camera.getModelViewMatrix(transform.position, transform.rotation, transform.scale, view)
            fontRenderer.tint = shape.color
            fontRenderer.drawText(bitmapFont, "hello its me", 0, 0)
        }
        fontRenderer.end()
    }

    override fun discard() {
        renderer.discard()
    }
}
