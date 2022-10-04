package shenanigans.engine.ecs.components

import org.joml.Vector2d
import shenanigans.engine.ecs.Component

class Position constructor(val position: Vector2d, val rotation: Double): Component{
}