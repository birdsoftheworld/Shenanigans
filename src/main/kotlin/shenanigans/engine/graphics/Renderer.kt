package shenanigans.engine.graphics

import org.joml.Vector2i
import org.lwjgl.opengl.GL30C.*
import org.lwjgl.opengl.GLUtil
import shenanigans.engine.ecs.*
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.graphics.api.RenderSystem
import shenanigans.engine.graphics.api.renderer.AbstractRenderer
import shenanigans.engine.graphics.api.renderer.FontRenderer
import shenanigans.engine.graphics.api.renderer.ShapeRenderer
import shenanigans.engine.graphics.api.renderer.TextureRenderer
import shenanigans.engine.graphics.api.resource.FontRendererResource
import shenanigans.engine.graphics.api.resource.ShapeRendererResource
import shenanigans.engine.graphics.api.resource.TextureRendererResource
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.scene.Scene
import shenanigans.engine.window.Window
import shenanigans.engine.window.WindowResource
import java.lang.System
import kotlin.reflect.KClass

object Renderer {
    private lateinit var syncCameraSystem: SyncCameraSystem
    private lateinit var drawBackgroundSystem: DrawBackgroundSystem
    private lateinit var shapeSystem: ShapeSystem
    private lateinit var spriteSystem: SpriteSystem

    private lateinit var renderSystems: List<RenderSystem>

    private lateinit var renderResources: Resources

    private lateinit var textureRenderer: TextureRenderer
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var fontRenderer: FontRenderer

    fun init() {
        renderResources = Resources()

        textureRenderer = TextureRenderer()
        shapeRenderer = ShapeRenderer()
        fontRenderer = FontRenderer()

        renderResources.set(TextureRendererResource(textureRenderer))
        renderResources.set(ShapeRendererResource(shapeRenderer))
        renderResources.set(FontRendererResource(fontRenderer))

        GlobalRendererState.initialize()
        if(System.getProperty("render_debug") != null) {
            GLUtil.setupDebugMessageCallback()
        }
        TextureManager.initialize()
        glEnable(GL_MULTISAMPLE)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        syncCameraSystem = SyncCameraSystem()
        drawBackgroundSystem = DrawBackgroundSystem()
        shapeSystem = ShapeSystem()
        spriteSystem = SpriteSystem()

        renderSystems = listOf(syncCameraSystem, drawBackgroundSystem, shapeSystem, spriteSystem)
    }

    fun discard() {
        textureRenderer.discard()
        shapeRenderer.discard()
        fontRenderer.discard()
        for (renderSystem in renderSystems) {
            renderSystem.discard()
        }
        TextureManager.discard()
    }

    fun renderGame(window: Window, scene: Scene, engineResources: Resources) {
        val width = window.width
        val height = window.height
        glViewport(0, 0, width, height)

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        val resources = ResourcesView(renderResources, scene.sceneResources, engineResources)
        scene.runSystems(resources, renderSystems)

        window.swapBuffers()
    }
}

private class DrawBackgroundSystem : RenderSystem {
    val background = TextureManager.createTexture("/background.png", TextureOptions(wrapping = TextureOptions.WrappingType.REPEAT))
    val imageSize = Vector2i(400, 400)

    init {

    }

    override fun query(): Iterable<KClass<out Component>> {
        return emptySet()
    }

    override fun execute(resources: ResourcesView, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val textureRenderer = resources.get<TextureRendererResource>().textureRenderer
        val size = resources.get<WindowResource>().window.size
        val camera = resources.get<CameraResource>().camera!!
        val translation = camera.translation

        val view = camera.computeViewMatrix()
        textureRenderer.projection = camera.computeProjectionMatrix()
        textureRenderer.start()

        textureRenderer.transformation = view
        textureRenderer.textureRect(translation.x, translation.y, size.x.toFloat(), size.y.toFloat(), background.getRegion(
            translation.x / imageSize.x, translation.y / imageSize.y, size.x.toFloat() / imageSize.x, size.y.toFloat() / imageSize.y
        ))

        textureRenderer.end()
    }
}

private class SyncCameraSystem : RenderSystem {
    override fun query(): Iterable<KClass<out Component>> {
        return emptySet()
    }

    override fun execute(resources: ResourcesView, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val window = resources.get<WindowResource>()
        resources.get<CameraResource>().camera?.setScreenSize(window.window.width, window.window.height)
    }
}