package shenanigans.engine.init.client

import shenanigans.engine.graphics.api.RenderSystem
import shenanigans.engine.init.EngineOptions
import shenanigans.engine.init.SystemList

data class ClientEngineOptions(val renderSystems: SystemList<RenderSystem> = ClientEngineOptionDefaults.DEFAULT_RENDER) : EngineOptions()