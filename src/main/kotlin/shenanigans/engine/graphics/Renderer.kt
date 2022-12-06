package shenanigans.engine.graphics

import org.lwjgl.opengl.GL30C.*
import org.lwjgl.opengl.GLUtil
import shenanigans.engine.ecs.*
import shenanigans.engine.graphics.api.CameraResource
import shenanigans.engine.graphics.api.RenderSystem
import shenanigans.engine.graphics.api.texture.TextureManager
import shenanigans.engine.scene.Scene
import shenanigans.engine.util.camera.OrthoCamera
import shenanigans.engine.window.Window
import shenanigans.engine.window.WindowResource
import java.lang.System
import kotlin.reflect.KClass

object Renderer {
    private val syncCameraSystem = SyncCameraSystem()
    private val shapeSystem = ShapeSystem()
    private val spriteSystem = SpriteSystem()

    private val renderSystems = listOf(syncCameraSystem, shapeSystem, spriteSystem)

    private lateinit var renderResources: Resources

    fun init() {
        renderResources = Resources()

        GlobalRendererState.initialize()
        if(System.getProperty("render_debug") != null) {
            GLUtil.setupDebugMessageCallback()
        }
        TextureManager.initialize()
        glEnable(GL_MULTISAMPLE)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    fun discard() {
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

private class SyncCameraSystem : RenderSystem {
    override fun discard() {

    }

    override fun query(): Iterable<KClass<out Component>> {
        return emptySet()
    }

    override fun execute(resources: ResourcesView, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val window = resources.get<WindowResource>()
        resources.get<CameraResource>().camera?.setScreenSize(window.window.width, window.window.height)
    }
}