package shenanigans.game.network
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import com.esotericsoftware.minlog.Log
import shenanigans.engine.HeadlessEngine
import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntityView
import shenanigans.engine.scene.Scene
import java.util.logging.Level

object Server {
    private val server : Server = Server()

    val engine: HeadlessEngine = HeadlessEngine(Scene())

    init {
        server.start()

        registerClasses(server)

        server.bind(40506,40506)

        addListeners()
    }

    private fun addListeners() {
        server.addListener(object : Listener {
            override fun received(connection: Connection?, thing: Any) {
                if (thing is Array<*> && thing.firstOrNull() is Component) {

                }
            }
        })
    }

    fun stop() {
        engine.running = false
        server.stop()
    }
}

fun main() {
    Server
}