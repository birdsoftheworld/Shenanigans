package shenanigans.engine.graphics

import org.lwjgl.opengl.GL30C.*
import org.lwjgl.opengl.GLUtil
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.api.renderer.FontRenderer
import shenanigans.engine.graphics.api.renderer.ShapeRenderer
import shenanigans.engine.graphics.api.renderer.TextureRenderer
import shenanigans.engine.graphics.api.resource.FontRendererResource
import shenanigans.engine.graphics.api.resource.ShapeRendererResource
import shenanigans.engine.graphics.api.resource.TextureRendererResource
import shenanigans.engine.graphics.api.system.ShapeSystem
import shenanigans.engine.graphics.api.system.SpriteSystem
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.scene.Scene
import shenanigans.engine.timer.TimerSystem
import shenanigans.engine.ui.UISystem
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.window.Window
import java.lang.System as JSystem

object Renderer {
    private lateinit var renderResources: Resources

    private lateinit var textureRenderer: TextureRenderer
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var fontRenderer: FontRenderer

    private val builtinSystems: List<System> = listOf(
        SpriteSystem(),
        ShapeSystem(),
        UISystem(),
        TimerSystem
    )

    fun init() {
        renderResources = Resources()

        textureRenderer = TextureRenderer()
        shapeRenderer = ShapeRenderer()
        fontRenderer = FontRenderer()

        renderResources.set(TextureRendererResource(textureRenderer))
        renderResources.set(ShapeRendererResource(shapeRenderer))
        renderResources.set(FontRendererResource(fontRenderer))

        GlobalRendererState.initialize()
        if (JSystem.getProperty("render_debug") != null) {
            GLUtil.setupDebugMessageCallback()
        }
        TextureManager.initialize()
    }

    fun discard() {
        textureRenderer.discard()
        shapeRenderer.discard()
        fontRenderer.discard()
        TextureManager.discard()
    }

    fun renderGame(
        window: Window,
        scene: Scene,
        engineResources: Resources,
        eventQueues: EventQueues<LocalEventQueue>
    ) {
        val width = window.width
        val height = window.height
        glViewport(0, 0, width, height)
        engineResources.get<CameraResource>().camera?.setScreenSize(width, height)

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        val resources = ResourcesView(renderResources, scene.sceneResources, engineResources)

        glEnable(GL_MULTISAMPLE)

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_LESS)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        builtinSystems.forEach(scene.runSystem(System::executeRender, resources, eventQueues))
        scene.defaultSystems.forEach(scene.runSystem(System::executeRender, resources, eventQueues))

        window.swapBuffers()
    }
}