package shenanigans.game.network

import shenanigans.engine.ClientOnly
import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntityView

class EntityPacket (val id: Int, entityView: EntityView, serverTimeMillis : Int): Packet(serverTimeMillis) {
    val components: MutableList<Component> = mutableListOf<Component>()

    init {
        entityView.components.values.forEach {
            if(!it.component.javaClass.isAnnotationPresent(ClientOnly::class.java)) {
                components.add(it.component)
            }
        }
    }
}