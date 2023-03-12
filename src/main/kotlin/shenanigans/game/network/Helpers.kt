package shenanigans.game.network

import shenanigans.engine.ecs.Component

@ClientOnly
class Synchronized : Component {
    var registration : RegistrationStatus = RegistrationStatus.Disconnected
}

enum class RegistrationStatus {
    Disconnected,
    Sent,
    Registered
}