package shenanigans.engine.ui.elements

import org.joml.Vector2f
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.font.BitmapFont
import shenanigans.engine.graphics.api.font.Font
import shenanigans.engine.graphics.api.resource.FontRendererResource
import shenanigans.engine.graphics.api.resource.ShapeRendererResource
import shenanigans.engine.util.camera.CameraResource

open class Text : Node() {
    var text: String = ""
        set(value) {
            field = value
            updateLayout()
        }

    var fontSize: Float = 16f
        set(value) {
            field = value
            updateLayout()
        }

    private lateinit var bmFont: BitmapFont

    var backgroundColor: Color? = null
    var color: Color = Color(0f, 0f, 0f)

    init {
        updateLayout()
    }

    private fun updateLayout() {
        bmFont = bitmapFontCache.getOrPut(fontSize) {
            notoSans.createSized(fontSize)
        }
        size = Vector2f(
            bmFont.measureText(text),
            bmFont.verticalMetrics.ascent + bmFont.verticalMetrics.descent
        )
    }

    override fun render(resources: ResourcesView) {
        val layout = getLayout()
        val camera = resources.get<CameraResource>().camera!!

        if (backgroundColor !== null) {
            val shapeRenderer = resources.get<ShapeRendererResource>().shapeRenderer

            shapeRenderer.start()
            shapeRenderer.projection = camera.computeProjectionMatrix()
            shapeRenderer.rect(layout.position.x(), layout.position.y(), layout.size.x(), layout.size.y(), color)
            shapeRenderer.end()
        }

        if (text !== "") {
            val fontRenderer = resources.get<FontRendererResource>().fontRenderer

            fontRenderer.start()
            fontRenderer.projection = camera.computeProjectionMatrix()
            fontRenderer.tint = color
            fontRenderer.drawText(
                bmFont,
                text,
                layout.position.x().toInt(),
                (layout.position.y() + bmFont.verticalMetrics.ascent).toInt()
            )
            fontRenderer.end()
        }
    }

    companion object {
        private val notoSans = Font.fromFile("/NotoSans-Medium.ttf")
        private val bitmapFontCache = mutableMapOf<Float, BitmapFont>()
    }
}