package shenanigans.game.network

import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import shenanigans.engine.ClientOnly
import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntityView
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

    fun sendEntity(entityView: EntityView) {
        val entity = mutableListOf<Component>()
        entityView.components.values.forEach {
            if(!it.component.javaClass.isAnnotationPresent(ClientOnly::class.java)) {
                entity.add(it.component)
            }
        }
        client.sendTCP(entity.toTypedArray())
    }

    private fun addListeners() {
        client.addListener(object : Listener {

        })
    }

}