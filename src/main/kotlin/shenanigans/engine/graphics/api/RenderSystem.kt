package shenanigans.engine.graphics.api

import shenanigans.engine.ecs.System

interface RenderSystem : System {
    fun discard() {}
}