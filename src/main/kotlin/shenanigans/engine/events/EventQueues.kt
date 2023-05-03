package shenanigans.engine.events

import shenanigans.engine.net.NetworkEventQueue
import kotlin.reflect.KProperty

interface EventQueues<Q : EventQueue?> {
    val physics: LocalEventQueue
    val network: NetworkEventQueue
    val render: LocalEventQueue
    val own: Q
}

internal data class RealEventQueues<Q : EventQueue?>(
    override val physics: LocalEventQueue,
    override val network: NetworkEventQueue,
    override val render: LocalEventQueue,
    override val own: Q
) : EventQueues<Q>

internal object FakeEventQueues : EventQueues<LocalEventQueue> {
    override val network: NetworkEventQueue by FakeProperty
    override val physics: LocalEventQueue by FakeProperty
    override val render: LocalEventQueue by FakeProperty
    override val own: LocalEventQueue by FakeProperty

    private object FakeProperty {
        inline operator fun <reified T> getValue(thisRef: FakeEventQueues, property: KProperty<*>): T {
            throw IllegalStateException("Event queue ${property.name} is not available in this context")
        }
    }
}

fun <Q : EventQueue?> fakeEventQueues(): EventQueues<Q> {
    return FakeEventQueues as EventQueues<Q>
}

fun <Q : EventQueue?> eventQueues(
    physics: LocalEventQueue,
    network: NetworkEventQueue,
    render: LocalEventQueue,
    own: Q
): EventQueues<Q> {
    return RealEventQueues(physics, network, render, own)
}