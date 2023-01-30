package shenanigans.engine.network.server
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import shenanigans.engine.HeadlessEngine
import shenanigans.engine.ecs.Resource
import shenanigans.engine.events.Event
import shenanigans.engine.scene.Scene
import shenanigans.game.network.ConnectionEvent
import shenanigans.game.network.EntityPacket
import shenanigans.game.network.registerClasses
import com.esotericsoftware.kryonet.Server as KServer

object Server : Resource {
    private val kServer : KServer = KServer(64 * 1024, 64 * 1024)

    val engine: HeadlessEngine

    init {
        engine = HeadlessEngine(makeScene())
    }

    fun run() {
        engine.engineResources.set(Server)
        registerClasses(kServer)

        kServer.start()

        kServer.bind(40506,40506)

        addListeners()

        engine.run()
    }

    private fun makeScene(): Scene {
        val scene = Scene()

        scene.defaultSystems.add(EntityUpdateSystem())
        scene.defaultSystems.add(ServerRegistrationSystem())
        scene.defaultSystems.add(FullEntitySyncSystem())

        return scene
    }

    private fun addListeners() {
        kServer.addListener(object : Listener {
            override fun received(connection: Connection?, thing: Any) {
                if (thing is Event) {
                    engine.queueEvent(thing)
                }
            }

            override fun connected(connection: Connection?) {
                if (connection != null) {
                    engine.queueEvent(ConnectionEvent(connection))
                }
            }
        })
    }

    fun sendReliable(event : Event) {
        kServer.sendToAllTCP(event)
    }

    fun sendUnreliable(entityPacket: EntityPacket) {
        kServer.sendToAllUDP(entityPacket) // TCP waits for received message and can crash the server
    }

    fun stop() {
        engine.running = false
        kServer.stop()
    }
}



fun main() {
    Server.run()
}