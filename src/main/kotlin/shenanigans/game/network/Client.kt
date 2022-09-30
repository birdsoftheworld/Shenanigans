package shenanigans.game.network
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener

object Client {
    val client : Client = Client()

    init {
        client.start()
        client.connect(5000, "192.168.0.4", 54555, 54777)

        client.sendTCP("test")
    }

    fun addListeners() {
        client.addListener(object : Listener {
            override fun received(connection: Connection?, `object`: Any) {
                // Type handlers
            }
        })
    }

}