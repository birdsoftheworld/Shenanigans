package shenanigans.game.network.server
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import shenanigans.engine.HeadlessEngine
import shenanigans.engine.ecs.Resource
import shenanigans.engine.scene.Scene
import shenanigans.game.network.EntityRegistrationPacket
import shenanigans.game.network.Packet
import shenanigans.game.network.registerClasses
import com.esotericsoftware.kryonet.Server as KServer

object Server : Resource {
    private val kServer : KServer = KServer()

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

        return scene
    }

    private fun addListeners() {
        kServer.addListener(object : Listener {
            override fun received(connection: Connection?, thing: Any) {
                if (thing is Packet) {
                    engine.queueEvent(thing)
                }
            }
        })
    }

    fun registerEntity(entityRegistrationPacket: EntityRegistrationPacket) {
        kServer.sendToAllTCP(entityRegistrationPacket)
    }

    fun stop() {
        engine.running = false
        kServer.stop()
    }
}



fun main() {
    Server.run()
}