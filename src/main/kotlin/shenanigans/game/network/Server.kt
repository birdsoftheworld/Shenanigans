package shenanigans.game.network
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import shenanigans.engine.ecs.EntityView
import shenanigans.engine.scene.Scene

object Server {
    val server : Server = Server()

    var scene : Scene = Scene()

    private var running: Boolean = true

    init {
        server.start()

        server.bind(40506,40506)

        addListeners()
    }

    private fun addListeners() {
        server.addListener(object : Listener {
            override fun received(connection: Connection?, thing: Any) {
                if (thing is EntityView) {
                    println(thing)
                }
            }
        })
    }

    fun stop() {
        running = false
        server.stop()
    }
}

fun main() {
    Server
}