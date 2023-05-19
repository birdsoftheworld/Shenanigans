package shenanigans.engine.ui.elements

import org.joml.Vector2f
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.font.BitmapFont
import shenanigans.engine.graphics.api.font.Font
import shenanigans.engine.graphics.api.resource.FontRendererResource
import shenanigans.engine.graphics.api.resource.ShapeRendererResource
import shenanigans.engine.util.camera.CameraResource

open class Text : ColoredBox() {
    var text: String = ""
        set(value) {
            field = value
            size = Vector2f(
                bmFont.measureText(text),
                bmFont.verticalMetrics.ascent + bmFont.verticalMetrics.descent
            )
        }

    var fontSize: Float = 16f
        set(value) {
            field = value
            bmFont = notoSans.createSized(fontSize)
        }
    private var bmFont: BitmapFont

    var backgroundColor: Color? = null

    init {
        bmFont = notoSans.createSized(fontSize)
        size = Vector2f(
            bmFont.measureText(text),
            bmFont.verticalMetrics.ascent + bmFont.verticalMetrics.descent
        )
    }

    override fun render(resources: ResourcesView, layout: Layout, z: Float) {
        val camera = resources.get<CameraResource>().camera!!

        if (backgroundColor !== null) {
            val shapeRenderer = resources.get<ShapeRendererResource>().shapeRenderer

            shapeRenderer.start()
            shapeRenderer.projection = camera.computeProjectionMatrix()
            shapeRenderer.rect(layout.position.x(), layout.position.y(), z, layout.size.x(), layout.size.y(), color!!)
            shapeRenderer.end()
        }

        if (text !== "") {
            val fontRenderer = resources.get<FontRendererResource>().fontRenderer

            fontRenderer.start()
            fontRenderer.projection = camera.computeProjectionMatrix()
            fontRenderer.tint = color ?: Color(0f, 0f, 0f)
            fontRenderer.drawText(
                bmFont,
                text,
                layout.position.x().toInt(),
                (layout.position.y() + bmFont.verticalMetrics.ascent).toInt(),
                z
            )
            fontRenderer.end()
        }
    }

    companion object {
        private val notoSans = Font.fromFile("/NotoSans-Medium.ttf")
    }
}