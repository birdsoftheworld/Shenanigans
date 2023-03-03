package shenanigans.game.network

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.DefaultSerializers.CollectionsEmptyMapSerializer
import com.esotericsoftware.kryo.serializers.DefaultSerializers.CollectionsSingletonMapSerializer
import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.net.EventMessage
import shenanigans.engine.net.MessageDelivery
import shenanigans.engine.net.events.ConnectionEvent
import shenanigans.engine.net.events.ConnectionEventType
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import java.util.*
import kotlin.jvm.internal.ClassReference

annotation class ClientOnly

internal fun registerDefaultClasses(kryo: Kryo) {
    // Components
    kryo.register(Component::class.java)
    kryo.register(Shape::class.java).setInstantiator { Shape(arrayOf(), Color(0f, 0f, 0f)) }
    kryo.register(Collider::class.java).setInstantiator { Collider(arrayOf(), static = false, triggerCollider = false) }
    kryo.register(Transform::class.java)
    kryo.register(Synchronized::class.java)

    // Events
    kryo.register(EventMessage::class.java).setInstantiator { EventMessage(ConnectionEvent(null, ConnectionEventType.Connect)) }
    kryo.register(MessageDelivery::class.java)
    kryo.register(EntityMovementPacket::class.java).setInstantiator { EntityMovementPacket(mapOf()) }
    kryo.register(EntityRegistrationPacket::class.java).setInstantiator { EntityRegistrationPacket(UUID.randomUUID(), mapOf()) }

    // Utils
    kryo.register(Map::class.java)
    kryo.register(ArrayList::class.java)
    kryo.register(Vector2f::class.java)
    kryo.register(Array<Vector2f>::class.java)
    kryo.register(Array<Component>::class.java)
    kryo.register(UUID::class.java).setInstantiator { UUID.randomUUID() }
    kryo.register(LinkedHashMap::class.java)
    kryo.register(ClassReference::class.java).setInstantiator { ClassReference(Void::class.java) }
    kryo.register(Class::class.java)
    kryo.register(Color::class.java).setInstantiator { Color(0f, 0f, 0f) }
    kryo.register(emptyMap<Unit, Unit>()::class.java, CollectionsEmptyMapSerializer())
    kryo.register(Collections.singletonMap<Unit, Unit>(null, null)::class.java, CollectionsSingletonMapSerializer())
}