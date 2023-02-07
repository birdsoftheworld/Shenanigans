package shenanigans.engine.net

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import shenanigans.engine.net.events.ConnectionEvent
import shenanigans.engine.net.events.ConnectionType
import shenanigans.game.network.registerDefaultClasses
import java.io.IOException
import com.esotericsoftware.kryonet.Client as KryoClient
import com.esotericsoftware.kryonet.Server as KryoServer


interface NetworkImplementation {
    fun sendMessage(msg: Message)
    fun registerListener(listener: (Message) -> Unit)
}

class Server(private val kryoServer: KryoServer) : NetworkImplementation {

    constructor() : this(KryoServer()) {
        registerDefaultClasses(kryoServer)

        kryoServer.start()
        kryoServer.bind(40506, 40506)
    }

    override fun sendMessage(msg: Message) {
        when (msg.delivery) {
            MessageDelivery.UnreliableUnordered -> kryoServer.sendToAllUDP(msg)
            MessageDelivery.ReliableOrdered -> kryoServer.sendToAllUDP(msg)
        }
    }

    override fun registerListener(listener: (Message) -> Unit) {
        kryoServer.addListener(object : Listener {
            override fun received(connection: Connection?, `object`: Any?) {
                if (`object` is Message) {
                    listener(`object`)
                }
            }

            override fun connected(connection: Connection?) {
                listener(EventMessage(ConnectionEvent(connection, ConnectionType.Connect)))
            }

            override fun disconnected(connection: Connection?) {
                listener(EventMessage(ConnectionEvent(connection, ConnectionType.Disconnect)))
            }
        })
    }
}

class Client(private val kryoClient: KryoClient) : NetworkImplementation {

    constructor() : this(KryoClient()) {
        registerDefaultClasses(kryoClient)

        try {
            val addresses = kryoClient.discoverHosts(40506, 500)
            if (addresses.size == 0) {
                throw RuntimeException("No servers found!")
            }
            kryoClient.connect(4500, addresses[0], 40506, 40506)
            println("Connecting to " + addresses[0])
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun sendMessage(msg: Message) {
        when (msg.delivery) {
            MessageDelivery.UnreliableUnordered -> kryoClient.sendUDP(msg)
            MessageDelivery.ReliableOrdered -> kryoClient.sendTCP(msg)
        }
    }

    override fun registerListener(listener: (Message) -> Unit) {
        kryoClient.addListener(object : Listener {
            override fun received(connection: Connection?, `object`: Any?) {
                if (`object` is Message) {
                    listener(`object`)
                }
            }

            override fun connected(connection: Connection?) {
                listener(EventMessage(ConnectionEvent(connection, ConnectionType.Connect)))
            }

            override fun disconnected(connection: Connection?) {
                listener(EventMessage(ConnectionEvent(connection, ConnectionType.Disconnect)))
            }
        })
    }
}
