package shenanigans.engine.init.client

import shenanigans.engine.graphics.api.RenderSystem
import shenanigans.engine.init.SystemList
import shenanigans.engine.graphics.api.system.DrawBackgroundSystem
import shenanigans.engine.graphics.api.system.ShapeSystem
import shenanigans.engine.graphics.api.system.SpriteSystem

object ClientEngineOptionDefaults {
    val MINIMAL_RENDER: SystemList<RenderSystem> = SystemList(listOf(::DrawBackgroundSystem))
    val DEFAULT_RENDER: SystemList<RenderSystem> = SystemList(listOf(::SpriteSystem, ::ShapeSystem), MINIMAL_RENDER)
}