package shenanigans.engine.graphics.api.component

import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.util.shapes.Polygon

/**
 * a shape to be rendered by `ShapeSystem`
 */

data class Shape(
    val polygon : Polygon,
    var color: Color,
) : Component