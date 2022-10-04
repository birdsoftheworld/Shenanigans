package shenanigans.game.network
import com.esotericsoftware.kryonet.Server
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener


object Server {
    val server : Server = Server()

    init {
        server.start()
        server.bind(40506,40506)
        addListeners()
    }

    private fun addListeners() {
        server.addListener(object : Listener {
            override fun received(connection: Connection?, `object`: Any) {
                if(`object` is String){
                    server.sendToAllTCP("Server Received: $`object`  \nServer Replied: COOL")
                }
            }
        })
    }

}