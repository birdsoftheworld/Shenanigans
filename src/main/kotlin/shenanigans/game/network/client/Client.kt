package shenanigans.game.network.client

import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import shenanigans.engine.ecs.EntityView
import shenanigans.game.network.EntityPacket
import shenanigans.game.network.EntityRegistrationPacket
import shenanigans.game.network.registerClasses
import java.io.IOException

object Client {
    private val client : Client = Client()

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

        registerClasses(client)

        addListeners()
    }

    fun sendEntity(entityView: EntityView, id: Int) {
        client.sendTCP(EntityPacket(id, entityView, 0))
    }

    private fun addListeners() {
        client.addListener(object : Listener {
            override fun received(connection: Connection?, thing: Any?) {

            }
        })

    }

    fun createNetworkedEntity(entityView: EntityView) {
        client.sendTCP(EntityRegistrationPacket(entityView, client.id, 0))
    }
}