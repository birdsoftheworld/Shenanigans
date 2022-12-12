package shenanigans.engine.graphics

import org.lwjgl.opengl.GL30C.*
import org.lwjgl.opengl.GLUtil
import shenanigans.engine.ecs.*
import shenanigans.engine.util.camera.CameraResource
import shenanigans.engine.graphics.api.RenderSystem
import shenanigans.engine.graphics.api.renderer.FontRenderer
import shenanigans.engine.graphics.api.renderer.ShapeRenderer
import shenanigans.engine.graphics.api.renderer.TextureRenderer
import shenanigans.engine.graphics.api.resource.FontRendererResource
import shenanigans.engine.graphics.api.resource.ShapeRendererResource
import shenanigans.engine.graphics.api.resource.TextureRendererResource
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.init.SystemList
import shenanigans.engine.scene.Scene
import shenanigans.engine.window.Window
import java.lang.System

object Renderer {
    private lateinit var renderSystems: List<RenderSystem>

    private lateinit var renderResources: Resources

    private lateinit var textureRenderer: TextureRenderer
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var fontRenderer: FontRenderer

    fun init(systems: SystemList<RenderSystem>) {
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

        renderSystems = systems.build()
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
        engineResources.get<CameraResource>().camera?.setScreenSize(width, height)

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        val resources = ResourcesView(renderResources, scene.sceneResources, engineResources)
        scene.runSystems(resources, renderSystems)

        window.swapBuffers()
    }
}