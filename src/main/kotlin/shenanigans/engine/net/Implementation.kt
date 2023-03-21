package shenanigans.engine.net

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import shenanigans.engine.net.events.ConnectionEvent
import shenanigans.engine.net.events.ConnectionEventType
import shenanigans.engine.term.Logger
import com.esotericsoftware.kryonet.Client as KryoClient
import com.esotericsoftware.kryonet.Server as KryoServer


interface NetworkImplementation {
    fun sendMessage(msg: Message)

    fun getEndpoint(): MessageEndpoint

    fun registerListener(listener: (Message) -> Unit)
    fun registerSendable(sendable: SendableClass<out Any>)
}

class Server(private val kryoServer: KryoServer) : NetworkImplementation {

    constructor() : this(KryoServer()) {
        kryoServer.start()
        kryoServer.bind(40506, 40506)
    }

    override fun sendMessage(msg: Message) {
        val connId: Int? = when (msg.recipient) {
            is MessageEndpoint.Client -> (msg.recipient as MessageEndpoint.Client).id
            is MessageEndpoint.Server -> throw RuntimeException("server cannot send message to itself")
            else -> null
        }

        if (connId !== null) {
            when (msg.delivery) {
                MessageDelivery.UnreliableUnordered -> kryoServer.sendToUDP(connId, msg)
                MessageDelivery.ReliableOrdered -> kryoServer.sendToTCP(connId, msg)
            }
        } else {
            when (msg.delivery) {
                MessageDelivery.UnreliableUnordered -> kryoServer.sendToAllUDP(msg)
                MessageDelivery.ReliableOrdered -> kryoServer.sendToAllUDP(msg)
            }
        }
    }

    override fun getEndpoint(): MessageEndpoint = MessageEndpoint.Server

    override fun registerListener(listener: (Message) -> Unit) {
        kryoServer.addListener(KryoListener(listener))
    }

    override fun registerSendable(sendable: SendableClass<out Any>) {
        sendable.registerKryo(kryoServer.kryo)
    }

    private class KryoListener(val cb: (Message) -> Unit) : Listener {
        override fun received(connection: Connection?, `object`: Any?) {
            if (`object` is Message) {
                `object`.sender = if (connection !== null) MessageEndpoint.Client(connection.id) else null
            }
        }

        override fun connected(connection: Connection?) {
            cb(
                EventMessage(
                    ConnectionEvent(
                        if (connection !== null) MessageEndpoint.Client(connection.id) else null,
                        ConnectionEventType.Connect
                    )
                )
            )
        }

        override fun disconnected(connection: Connection?) {
            cb(
                EventMessage(
                    ConnectionEvent(
                        if (connection !== null) MessageEndpoint.Client(connection.id) else null,
                        ConnectionEventType.Disconnect
                    )
                )
            )
        }
    }
}

class Client(private val kryoClient: KryoClient) : NetworkImplementation {

    constructor() : this(KryoClient()) {
        kryoClient.start()

        try {
            val addresses = kryoClient.discoverHosts(40506, 500)
            if (addresses.size == 0) {
                throw RuntimeException("No servers found!")
            }
            kryoClient.connect(4500, addresses[0], 40506, 40506)
            println("Connecting to " + addresses[0])
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun sendMessage(msg: Message) {
        if (msg.recipient is MessageEndpoint.Client) {
            throw RuntimeException("client cannot send message to other clients")
        }

        when (msg.delivery) {
            MessageDelivery.UnreliableUnordered -> kryoClient.sendUDP(msg)
            MessageDelivery.ReliableOrdered -> kryoClient.sendTCP(msg)
        }
    }

    override fun getEndpoint(): MessageEndpoint = MessageEndpoint.Client(kryoClient.id)

    override fun registerListener(listener: (Message) -> Unit) {
        kryoClient.addListener(KryoListener(listener))
    }

    override fun registerSendable(sendable: SendableClass<out Any>) {
        sendable.registerKryo(kryoClient.kryo)
    }

    private class KryoListener(val cb: (Message) -> Unit) : Listener {
        override fun received(connection: Connection?, `object`: Any?) {
            if (`object` is Message) {
                if (`object`.sender == null) `object`.sender = MessageEndpoint.Server
            }
        }

        override fun connected(connection: Connection?) {
            cb(EventMessage(ConnectionEvent(MessageEndpoint.Server, ConnectionEventType.Connect)))
        }

        override fun disconnected(connection: Connection?) {
            cb(EventMessage(ConnectionEvent(MessageEndpoint.Server, ConnectionEventType.Disconnect)))
        }
    }
}