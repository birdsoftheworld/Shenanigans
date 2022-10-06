package shenanigans.engine.ecs.components

class Collider constructor(val vertices: DoubleArray, val static: Boolean, val triggerCollider: Boolean) {
    constructor(shape: Shape, static: Boolean, triggerCollider: Boolean) :
            this(shape.vertices, static, triggerCollider)
}