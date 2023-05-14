package shenanigans.game.player

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.net.MessageDelivery
import shenanigans.engine.net.MessageEndpoint
import shenanigans.engine.net.NetworkEventQueue
import shenanigans.engine.net.events.ConnectionEvent
import shenanigans.engine.net.events.ConnectionEventType
import shenanigans.engine.physics.Time
import shenanigans.game.state.Mode
import shenanigans.game.state.ModeChangeEvent
import kotlin.reflect.KClass

class PlayerManagementSystem : System{

    private val switchTime = 30;

    private var lastSwitch : Double = 0.0

    private val runners : ArrayDeque<MessageEndpoint> = ArrayDeque()
    private val builders : ArrayDeque<MessageEndpoint> = ArrayDeque()

    override fun executeNetwork(
            resources: ResourcesView,
            eventQueues: EventQueues<NetworkEventQueue>,
            query: (Iterable<KClass<out Component>>) -> QueryView,
            lifecycle: EntitiesLifecycle
    ) {
        val currentTime =  resources.get<Time>().currentTime

        eventQueues.own.receive(ConnectionEvent::class)
                .filter { it.type == ConnectionEventType.Connect }
                .forEach { connectionEvent ->
                    if(runners.size <= builders.size) {
                        runners.addLast(connectionEvent.endpoint!!)
                    }
                    else {
                        builders.add(connectionEvent.endpoint!!)
                        eventQueues.own.queueNetwork(
                                ModeChangeEvent(Mode.RUN, Mode.BUILD),
                                MessageDelivery.ReliableOrdered,
                                connectionEvent.endpoint
                        )
                    }
                }


        eventQueues.own.receive(ConnectionEvent::class)
                .filter { it.type == ConnectionEventType.Disconnect }
                .forEach { connectionEvent ->
                    if(runners.contains(connectionEvent.endpoint)) {
                        runners.remove(connectionEvent.endpoint)
                    }

                    if(builders.contains(connectionEvent.endpoint)) {
                        builders.remove(connectionEvent.endpoint)
                    }

                    if(builders.size > runners.size) {
                        eventQueues.own.queueNetwork(
                                ModeChangeEvent(Mode.BUILD, Mode.RUN),
                                MessageDelivery.ReliableOrdered,
                                builders.first()
                        )

                        runners.addLast(builders.removeFirst())
                    }

                    if(runners.size - 1 > builders.size) {
                        eventQueues.own.queueNetwork(
                                ModeChangeEvent(Mode.RUN, Mode.BUILD),
                                MessageDelivery.ReliableOrdered,
                                runners.first()
                        )

                        builders.addLast(runners.removeFirst())
                    }
                }

        if(currentTime - lastSwitch > switchTime) {
            if(runners.size >= 1 && builders.size >= 1) {
                eventQueues.own.queueNetwork(
                        ModeChangeEvent(Mode.RUN, Mode.BUILD),
                        MessageDelivery.ReliableOrdered,
                        runners.first()
                )
                eventQueues.own.queueNetwork(
                        ModeChangeEvent(Mode.BUILD, Mode.RUN),
                        MessageDelivery.ReliableOrdered,
                        runners.first()
                )

                runners.addLast(builders.removeFirst())
                builders.addLast(runners.removeFirst())
            }

            lastSwitch = currentTime;
        }
    }
}