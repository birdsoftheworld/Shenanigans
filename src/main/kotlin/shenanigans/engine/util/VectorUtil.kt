package shenanigans.engine.util

import org.joml.Vector2f
import org.joml.Vector2ic

fun Vector2ic.toFloat() = Vector2f(this.x().toFloat(), this.y().toFloat())