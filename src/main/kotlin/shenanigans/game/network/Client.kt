package shenanigans.game.network

import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import shenanigans.engine.util.Transform
import java.io.IOException

object Client {
    val client : Client = Client()

    init {
        client.start()
        try {
            val addresses = client.discoverHosts(40506, 500)
            if (addresses.size == 0) {
                throw RuntimeException("No servers found!")
            }
            client.connect(4500, addresses[0], 40506, 40506)
            println("Connecting to " + addresses[0])
        } catch (e: IOException) {
            e.printStackTrace()
        }
        addListeners()
    }

    fun sendObj(msg : Transform){
        client.sendTCP(msg)
    }

    private fun addListeners() {
        client.addListener(object : Listener {
            override fun received(connection: Connection?, `object`: Any) {
                if (`object` is String) {
                    println(`object`)
                }
            }
        })
    }

}