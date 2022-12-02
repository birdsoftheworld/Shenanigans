package shenanigans.game.network

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryonet.EndPoint
import org.joml.Vector2f
import shenanigans.engine.ecs.Component
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import shenanigans.game.KeyboardPlayer


fun registerClasses(endpoint: EndPoint){
    val kryo: Kryo = endpoint.kryo
    kryo.register(Component::class.java)
    kryo.register(Map::class.java)
    kryo.register(Shape::class.java)
    kryo.register(Collider::class.java)
    kryo.register(Transform::class.java)
    kryo.register(Map::class.java)
    kryo.register(ArrayList::class.java)
    kryo.register(Vector2f::class.java)
    kryo.register(Sendable::class.java)
    kryo.register(KeyboardPlayer::class.java)
    kryo.register(Color::class.java)
    kryo.register(Array<Vector2f>::class.java)

}