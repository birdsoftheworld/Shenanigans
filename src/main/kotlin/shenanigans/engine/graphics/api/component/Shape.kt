package shenanigans.engine.graphics.api.component

import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.util.Polygon

/**
 * a shape to be rendered by `ShapeSystem`
 */
data class Shape(
    val polygon : Polygon,
    val color: Color,
) : Component