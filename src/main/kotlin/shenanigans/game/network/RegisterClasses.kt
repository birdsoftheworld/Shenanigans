package shenanigans.game.network

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryonet.EndPoint
import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntityView
import shenanigans.engine.ecs.EntityId
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform


fun registerClasses(endpoint: EndPoint){
    val kryo: Kryo = endpoint.kryo

    // Components
    kryo.register(Component::class.java)
    kryo.register(Shape::class.java).setInstantiator {Shape(arrayOf(), Color(0f, 0f, 0f))}
    kryo.register(Collider::class.java).setInstantiator {Collider(arrayOf(), static = false, triggerCollider = false)}
    kryo.register(Transform::class.java)
    kryo.register(Synchronized::class.java)
    kryo.register(EntityRegistrationPacket::class.java).setInstantiator {EntityRegistrationPacket(-1, -1)}
    kryo.register(EntityPacket::class.java)

    // Utils
    kryo.register(Map::class.java)
    kryo.register(ArrayList::class.java)
    kryo.register(Vector2f::class.java)
    kryo.register(Array<Vector2f>::class.java)
    kryo.register(Array<Component>::class.java)
    kryo.register(EntityId::class.java)
}