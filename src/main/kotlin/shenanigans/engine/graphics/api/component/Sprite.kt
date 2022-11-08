package shenanigans.engine.graphics.api.component

import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.api.texture.TextureRegion

data class Sprite(
    val sprite: TextureRegion,
    val size: Vector2f
) : Component