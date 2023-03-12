package shenanigans.engine.graphics.api.component

import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.api.texture.TextureRegion
import shenanigans.engine.util.shapes.Rectangle
import shenanigans.game.network.ClientOnly

@ClientOnly
data class Sprite(
    var sprite: TextureRegion,
    var rectangle: Rectangle,
) : Component