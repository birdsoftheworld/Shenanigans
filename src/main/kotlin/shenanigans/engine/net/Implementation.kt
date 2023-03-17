package shenanigans.engine.net

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import shenanigans.engine.net.events.ConnectionEvent
import shenanigans.engine.net.events.ConnectionEventType
import com.esotericsoftware.kryonet.Client as KryoClient
import com.esotericsoftware.kryonet.Server as KryoServer


interface NetworkImplementation {
    fun connect()

    fun sendMessage(msg: Message)
    fun registerListener(listener: (Message) -> Unit)
    fun registerSendable(sendable: SendableClass<out Any>)
}

class Server(private val kryoServer: KryoServer) : NetworkImplementation {

    constructor() : this(KryoServer())

    override fun connect() {
        kryoServer.start()
        kryoServer.bind(40506, 40506)
    }

    override fun sendMessage(msg: Message) {
        if (msg.recipient != null) {
            when (msg.delivery) {
                MessageDelivery.UnreliableUnordered -> kryoServer.sendToUDP(msg.recipient!!, msg)
                MessageDelivery.ReliableOrdered -> kryoServer.sendToTCP(msg.recipient!!, msg)
            }
        } else {
            when (msg.delivery) {
                MessageDelivery.UnreliableUnordered -> kryoServer.sendToAllUDP(msg)
                MessageDelivery.ReliableOrdered -> kryoServer.sendToAllUDP(msg)
            }
        }
    }

    override fun registerListener(listener: (Message) -> Unit) {
        kryoServer.addListener(KryoListener(listener))
    }

    override fun registerSendable(sendable: SendableClass<out Any>) {
        sendable.registerKryo(kryoServer.kryo)
    }
}

class Client(private val kryoClient: KryoClient) : NetworkImplementation {

    constructor() : this(KryoClient())

    override fun connect() {
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

    override fun registerSendable(sendable: SendableClass<out Any>) {
        sendable.registerKryo(kryoClient.kryo)
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
        cb(EventMessage(ConnectionEvent(connection?.id, ConnectionEventType.Connect)))
    }

    override fun disconnected(connection: Connection?) {
        cb(EventMessage(ConnectionEvent(connection?.id, ConnectionEventType.Disconnect)))
    }
}