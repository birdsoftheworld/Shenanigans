package shenanigans.game.network

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.EndPoint
import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntityId
import shenanigans.engine.events.Event
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import kotlin.jvm.internal.ClassReference

class Synchronized : Component {
    var serverId: EntityId? = null
}

class ConnectionEvent(val connection: Connection) : Event

fun registerClasses(endpoint: EndPoint){
    val kryo: Kryo = endpoint.kryo

    // Components
    kryo.register(Component::class.java)
    kryo.register(Shape::class.java).setInstantiator {Shape(arrayOf(), Color(0f, 0f, 0f))}
    kryo.register(Collider::class.java).setInstantiator {Collider(arrayOf(), static = false, triggerCollider = false)}
    kryo.register(Transform::class.java)
    kryo.register(Synchronized::class.java)

    // Utils
    kryo.register(Map::class.java)
    kryo.register(ArrayList::class.java)
    kryo.register(Vector2f::class.java)
    kryo.register(Array<Vector2f>::class.java)
    kryo.register(Array<Component>::class.java)
    kryo.register(EntityId::class.java).setInstantiator { EntityId(-1)}
    kryo.register(LinkedHashMap::class.java)
    kryo.register(ClassReference::class.java).setInstantiator { ClassReference(Void::class.java) }
    kryo.register(Class::class.java)
    kryo.register(Color::class.java).setInstantiator { Color(0f, 0f, 0f) }
}