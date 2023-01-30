package shenanigans.engine.ui.elements

import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.resource.ShapeRendererResource
import shenanigans.engine.util.camera.CameraResource

open class ColoredBox : Box() {
    var color: Color? = null

    override fun render(resources: ResourcesView, layout: Layout) {
        if (color !== null) {
            val shapeRenderer = resources.get<ShapeRendererResource>().shapeRenderer
            val camera = resources.get<CameraResource>().camera!!

            shapeRenderer.start()
            shapeRenderer.projection = camera.computeProjectionMatrix()
            shapeRenderer.rect(layout.position.x(), layout.position.y(), layout.size.x(), layout.size.y(), color!!)
            shapeRenderer.end()
        }
    }
}