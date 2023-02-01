package shenanigans.engine.network

import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import shenanigans.engine.ClientEngine
import shenanigans.engine.events.Event
import shenanigans.game.network.registerClasses
import java.io.IOException

class Client (val engine: ClientEngine) {
    private val kClient : Client = Client()

    init {
        registerClasses(kClient)

        kClient.start()

        try {
            val addresses = kClient.discoverHosts(40506, 500)
            if (addresses.size == 0) {
                throw RuntimeException("No servers found!")
            }
            kClient.connect(4500, addresses[0], 40506, 40506)
            println("Connecting to " + addresses[0])
        } catch (e: IOException) {
            e.printStackTrace()
        }

        addListeners()
    }

    private fun addListeners() {
        kClient.addListener(object : Listener {
            override fun received(connection: Connection?, thing: Any?) {
                if (thing is Event) {
                    engine.queueEvent(thing)
                }
            }
        })

    }

    fun sendReliable(event : Event) {
        kClient.sendTCP(event)
    }

    fun sendUnreliable(event: Event) {
        kClient.sendUDP(event) // TCP waits for received message and can crash the server
    }

    fun getId() : Int{
        return kClient.id
    }
}