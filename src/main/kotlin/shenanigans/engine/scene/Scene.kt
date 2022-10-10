package shenanigans.engine.scene

import shenanigans.engine.Engine
import shenanigans.engine.ecs.Entities

class Scene {
    val entities : Entities = Entities()

    init {
        Engine().run()
    }
}