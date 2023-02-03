package shenanigans.engine.net

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server as KryoServer
import com.esotericsoftware.kryonet.Client as KryoClient


interface NetworkImplementation {
    fun sendMessage(msg: Message)
    fun registerListener(listener: (Message) -> Unit)
}

class Server(private val kryoServer: KryoServer) : NetworkImplementation {
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
        })
    }
}

class Client(private val kryoClient: KryoClient) : NetworkImplementation {
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
        })
    }
}
