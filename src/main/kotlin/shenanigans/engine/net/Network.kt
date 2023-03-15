package shenanigans.engine.net

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.DefaultSerializers
import com.esotericsoftware.kryonet.Connection
import org.joml.Vector2f
import org.joml.Vector3f
import shenanigans.engine.ecs.Component
import shenanigans.engine.events.Event
import shenanigans.engine.events.EventQueue
import shenanigans.engine.graphics.TextureKey
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.graphics.api.texture.Texture
import shenanigans.engine.graphics.api.texture.TextureRegion
import shenanigans.engine.net.events.ConnectionEvent
import shenanigans.engine.net.events.ConnectionEventType
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import shenanigans.engine.util.shapes.Polygon
import shenanigans.engine.util.shapes.Rectangle
import shenanigans.game.network.EntityMovementPacket
import shenanigans.game.network.EntityRegistrationPacket
import shenanigans.game.network.Synchronized
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.concurrent.withLock
import kotlin.reflect.KClass
import kotlin.jvm.internal.ClassReference

class Network(
    internal val impl: NetworkImplementation,
    private val userSendables: Set<SendableClass<Any>> = emptySet()
) {
    internal var receivedMessages: MutableList<Message> = mutableListOf()
    internal val receiveLock: ReentrantLock = ReentrantLock()

    init {
        impl.registerListener { msg ->
            receiveLock.withLock {
                receivedMessages.add(msg)
            }
        }

        builtinSendables().union(userSendables).forEach(impl::registerSendable)
    }

    fun createEventQueue(): NetworkEventQueue {
        return NetworkEventQueue(this)
    }
}

class NetworkEventQueue internal constructor(val network: Network) : EventQueue() {
    var receivedMessages: List<EventMessage<*>> = emptyList()

    override val received: List<Event>
        get() = receivedMessages.map { it.event }

    fun <E : Event> receiveNetwork(cl: KClass<E>): Sequence<EventMessage<E>> {
        return receivedMessages.asSequence().filter { it.event::class == cl }.map { it as EventMessage<E> }
    }

    override fun queueLater(event: Event) {
        network.impl.sendMessage(EventMessage(event))
    }

    fun queueToConnection(connection: Connection, event: Event) {
        network.impl.sendMessageToConnection(connection, EventMessage(event))
    }

    fun queueNetwork(event: Event, delivery: MessageDelivery) {
        network.impl.sendMessage(EventMessage(event, delivery))
    }

    override fun finish() {
        network.receiveLock.withLock {
            receivedMessages =
                network.receivedMessages.filterIsInstance(EventMessage::class.java)
            network.receivedMessages.clear()
        }
    }
}

data class SendableClass<T : Any>(
    val cl: KClass<out T>,
    val instantiator: (() -> T)? = null,
    val serializer: Serializer<T>? = null
) {
    internal fun hash(): Int {
        return cl.qualifiedName.hashCode()
    }

    internal fun registerKryo(kryo: Kryo) {
        val registration = kryo.register(cl.java, serializer, hash())

        if (serializer == null) {
            registration.setInstantiator { instantiator?.invoke() }
        }
    }
}

fun builtinSendables(): Set<SendableClass<out Any>> {
    return setOf(
        // Components
        SendableClass(Component::class),
        SendableClass(Shape::class, instantiator = { Shape(Polygon(arrayOf()), Color(0f, 0f, 0f)) }),
        SendableClass(
            Sprite::class,
            instantiator = { Sprite(TextureRegion(Texture(TextureKey()), 0f, 0f, 0f, 0f), Rectangle(0f, 0f)) }),
        SendableClass(
            Collider::class,
            instantiator = { Collider(Polygon(arrayOf()), static = false, triggerCollider = false) }),
        SendableClass(Transform::class),
        SendableClass(Synchronized::class),
        SendableClass(Polygon::class, instantiator = { Polygon(arrayOf()) }),
        SendableClass(Rectangle::class, instantiator = { Rectangle(0f, 0f) }),

        // Events
        SendableClass(
            EventMessage::class,
            instantiator = { EventMessage(ConnectionEvent(null, ConnectionEventType.Connect)) }),
        SendableClass(MessageDelivery::class),
        SendableClass(EntityMovementPacket::class, instantiator = { EntityMovementPacket(mapOf()) }),
        SendableClass(
            EntityRegistrationPacket::class,
            instantiator = { EntityRegistrationPacket(UUID.randomUUID(), mapOf()) }),

        // Utils
        SendableClass(Map::class),
        SendableClass(ArrayList::class),
        SendableClass(Vector2f::class),
        SendableClass(Array<Vector2f>::class),
        SendableClass(Array<Component>::class),
        SendableClass(UUID::class, serializer = object : Serializer<UUID>() {
            override fun write(kryo: Kryo, output: Output, obj: UUID?) {
                obj!!
                output.writeLong(obj.mostSignificantBits)
                output.writeLong(obj.leastSignificantBits)
            }

            override fun read(kryo: Kryo, input: Input, type: Class<out UUID>): UUID {
                val msb = input.readLong()
                val lsb = input.readLong()
                return UUID(msb, lsb)
            }

            override fun getAcceptsNull(): Boolean {
                return false
            }
        }),
        SendableClass(LinkedHashMap::class),
        SendableClass(ClassReference::class, instantiator = { ClassReference(Unit::class.java) }),
        SendableClass(Class::class),
        SendableClass(Color::class, instantiator = { Color(0f, 0f, 0f) }),
        SendableClass(emptyMap<Unit, Unit>()::class, serializer = DefaultSerializers.CollectionsEmptyMapSerializer()),
        SendableClass(
            Collections.singletonMap<Unit, Unit>(null, null)::class,
            serializer = DefaultSerializers.CollectionsSingletonMapSerializer()
        ),
        SendableClass(Vector3f::class),
        SendableClass(Texture::class, instantiator = { Texture(TextureKey()) }),
        SendableClass(TextureKey::class),
        SendableClass(TextureRegion::class, instantiator = { TextureRegion(Texture(TextureKey()), 0f, 0f, 0f, 0f) }),
    )
}