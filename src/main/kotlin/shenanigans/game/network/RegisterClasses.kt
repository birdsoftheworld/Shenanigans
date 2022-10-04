package shenanigans.game.network

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryonet.EndPoint


fun RegisterClasses(endpoint: EndPoint){
    val kryo: Kryo = endpoint.getKryo()


}