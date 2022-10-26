package shenanigans.game.network

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryonet.EndPoint
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import java.awt.Component
import java.awt.Shape

fun registerClasses(endpoint: EndPoint){
    val kryo: Kryo = endpoint.getKryo()
    kryo.register(Component::class.java)
    kryo.register(Map::class.java)
    kryo.register(Shape::class.java)
    kryo.register(Collider::class.java)
    kryo.register(Transform::class.java)
}