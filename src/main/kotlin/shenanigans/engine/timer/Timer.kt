package shenanigans.engine.timer

import org.lwjgl.glfw.GLFW
import shenanigans.engine.ecs.*
import shenanigans.engine.events.Event
import shenanigans.engine.events.EventQueue
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.net.NetworkEventQueue
import java.util.*
import kotlin.reflect.KClass

fun timeEventPhysics(delay: Double, event: Event) {
    TimerSystem.physicsTimers[getTime() + delay] = event
}

fun timeEventNetwork(delay: Double, event: Event) {
    TimerSystem.networkTimers[getTime() + delay] = event
}

fun timeEventRender(delay: Double, event: Event) {
    TimerSystem.renderTimers[getTime() + delay] = event
}

object TimerSystem : System {
    internal val physicsTimers = sortedMapOf<Double, Event>()
    internal val networkTimers = sortedMapOf<Double, Event>()
    internal val renderTimers = sortedMapOf<Double, Event>()

    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        pollTimers(physicsTimers, eventQueues.physics)
    }

    override fun executeNetwork(
        resources: ResourcesView,
        eventQueues: EventQueues<NetworkEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        pollTimers(networkTimers, eventQueues.network)
    }

    override fun executeRender(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        pollTimers(renderTimers, eventQueues.render)
    }
}

internal fun getTime(): Double = GLFW.glfwGetTime()

internal fun pollTimers(timers: SortedMap<Double, out Event>, queue: EventQueue) {
    timers.headMap(getTime()).let { finished ->
        finished.values.forEach(queue::queueLater)
        finished.clear()
    }
}