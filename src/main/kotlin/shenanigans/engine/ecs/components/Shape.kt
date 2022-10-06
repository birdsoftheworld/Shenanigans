package shenanigans.engine.ecs.components

import shenanigans.engine.ecs.Component

class Shape constructor(val vertices : DoubleArray) : Component {

    val triangleMesh : MutableList<Array<Double>> = mutableListOf()
    
    init {
        for(i in 1 until (vertices.size - 1)) {
            triangleMesh.add(arrayOf<Double>(vertices[0], vertices[i], vertices[i+1]))
        }
    }
}