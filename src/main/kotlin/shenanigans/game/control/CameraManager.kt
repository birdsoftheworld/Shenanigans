package shenanigans.game.control

import org.joml.Vector3f
import org.joml.Vector3fc
import shenanigans.engine.ecs.Component
import shenanigans.engine.ecs.EntityView
import java.util.*

sealed class CameraMode

class StationaryCamera(val pos: Vector3fc) : CameraMode()

class MovableCamera(val pos: Vector3f) : CameraMode()

class FollowingCamera(val target: UUID, val func: (EntityView) -> Vector3fc) : CameraMode()

class CameraManager(var mode: CameraMode, var lastPosition: Vector3fc = Vector3f()) : Component