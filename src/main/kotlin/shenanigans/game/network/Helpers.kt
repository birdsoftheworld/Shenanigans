package shenanigans.game.network

import shenanigans.engine.ecs.Component

class Synchronized : Component {
    @Transient
    var registration = RegistrationStatus.Disconnected
    var ownerID : Int? = -1
}

enum class RegistrationStatus {
    Disconnected,
    Sent,
    Registered
}