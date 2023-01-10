package shenanigans.game.network.server
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import shenanigans.engine.HeadlessEngine
import shenanigans.engine.ecs.Resource
import shenanigans.engine.scene.Scene
import shenanigans.game.network.EntityRegistrationPacket
import shenanigans.game.network.Packet
import shenanigans.game.network.registerClasses

object Server : Resource {
    var packet = false

    private val server : Server = Server()

    val engine: HeadlessEngine

    init {
        engine = HeadlessEngine(makeScene())

        registerClasses(server)

        server.start()

        server.bind(40506,40506)

        addListeners()

        engine.run()
    }

    private fun makeScene(): Scene {
        val scene = Scene()

        scene.defaultSystems.add(EntityUpdateSystem())
        scene.defaultSystems.add(EntityRegistrationSystem())

        return scene
    }

    private fun addListeners() {
        server.addListener(object : Listener {
            override fun received(connection: Connection?, thing: Any) {
                if (thing is Packet) {
                    println(thing.javaClass)
                    packet = false
                    engine.queueEvent(thing)
                }
            }
        })
    }

    fun registerEntity(entityRegistrationPacket: EntityRegistrationPacket) {
        server.sendToTCP(entityRegistrationPacket.clientId, entityRegistrationPacket)
        println("return registration packet")
    }

    fun stop() {
        engine.running = false
        server.stop()
    }
}



fun main() {
    Server
}