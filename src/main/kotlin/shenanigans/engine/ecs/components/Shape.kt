package shenanigans.engine.ecs.components

import org.joml.Vector2f
import shenanigans.engine.ecs.Component

class Shape constructor(val vertices : Array<Vector2f>) : Component {

    private val triangleMesh : MutableList<Array<Vector2f>> = mutableListOf()
    
    init {
        for(i in 1 until (vertices.size - 1)) {
            triangleMesh.add(arrayOf<Vector2f>(vertices[0], vertices[i], vertices[i+1]))
        }
    }
}