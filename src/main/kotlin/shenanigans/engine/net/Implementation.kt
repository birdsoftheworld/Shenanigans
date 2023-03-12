package shenanigans.engine.net

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import shenanigans.engine.net.events.ConnectionEvent
import shenanigans.engine.net.events.ConnectionEventType
import shenanigans.game.network.registerDefaultClasses
import com.esotericsoftware.kryonet.Client as KryoClient
import com.esotericsoftware.kryonet.Server as KryoServer


interface NetworkImplementation {
    fun sendMessage(msg: Message)
    fun registerListener(listener: (Message) -> Unit)

    fun sendMessageToConnection(connection: Connection, msg: Message)
}

class Server(private val kryoServer: KryoServer) : NetworkImplementation {

    constructor() : this(KryoServer()) {
        registerDefaultClasses(kryoServer.kryo)

        kryoServer.start()
        kryoServer.bind(40506, 40506)
    }

    override fun sendMessage(msg: Message) {
        when (msg.delivery) {
            MessageDelivery.UnreliableUnordered -> kryoServer.sendToAllUDP(msg)
            MessageDelivery.ReliableOrdered -> kryoServer.sendToAllUDP(msg)
        }
    }

    override fun sendMessageToConnection(connection: Connection, msg: Message) {
        when (msg.delivery) {
            MessageDelivery.UnreliableUnordered -> connection.sendUDP(msg)
            MessageDelivery.ReliableOrdered -> connection.sendTCP(msg)
        }
    }

    override fun registerListener(listener: (Message) -> Unit) {
        kryoServer.addListener(KryoListener(listener))
    }
}

class Client(private val kryoClient: KryoClient) : NetworkImplementation {

    constructor() : this(KryoClient()) {
        registerDefaultClasses(kryoClient.kryo)

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

    override fun sendMessageToConnection(connection: Connection, msg: Message) {
        when (msg.delivery) {
            MessageDelivery.UnreliableUnordered -> connection.sendUDP(msg)
            MessageDelivery.ReliableOrdered -> connection.sendTCP(msg)
        }
    }

    override fun sendMessage(msg: Message) {
        when (msg.delivery) {
            MessageDelivery.UnreliableUnordered -> kryoClient.sendUDP(msg)
            MessageDelivery.ReliableOrdered -> kryoClient.sendTCP(msg)
        }
    }

    override fun registerListener(listener: (Message) -> Unit) {
        kryoClient.addListener(KryoListener(listener))

    }
}

internal class KryoListener(val cb: (Message) -> Unit) : Listener {
    override fun received(connection: Connection?, `object`: Any?) {
        if (`object` is Message) {
            `object`.sender = connection?.id
            cb(`object`)
        }
    }

    override fun connected(connection: Connection?) {
        cb(EventMessage(ConnectionEvent(connection, ConnectionEventType.Connect)))
    }

    override fun disconnected(connection: Connection?) {
        cb(EventMessage(ConnectionEvent(connection, ConnectionEventType.Disconnect)))
    }
}