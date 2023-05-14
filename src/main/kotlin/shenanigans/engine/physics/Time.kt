package shenanigans.engine.physics

import shenanigans.engine.ecs.Resource

class Time(val deltaTime: Double, val currentTime: Double) : Resource // Seconds