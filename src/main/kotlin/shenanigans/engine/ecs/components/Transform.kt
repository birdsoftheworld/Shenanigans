package shenanigans.engine.ecs.components

import org.joml.Vector2f
import shenanigans.engine.ecs.Component

class Transform(val position: Vector2f, val rotation: Float, val scale: Vector2f): Component