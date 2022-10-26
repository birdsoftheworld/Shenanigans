package shenanigans.game.network

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryonet.EndPoint
import shenanigans.engine.ecs.components.Collider
import shenanigans.engine.ecs.components.ShapeRender
import shenanigans.engine.ecs.components.Transform
import java.awt.Component
import java.awt.Shape
import javax.swing.text.html.parser.Entity

fun registerClasses(endpoint: EndPoint){
    val kryo: Kryo = endpoint.getKryo()
    kryo.register(Component::class.java)
    kryo.register(Map::class.java)
    kryo.register(Shape::class.java)
    kryo.register(Collider::class.java)
    kryo.register(ShapeRender::class.java)
    kryo.register(Transform::class.java)
}