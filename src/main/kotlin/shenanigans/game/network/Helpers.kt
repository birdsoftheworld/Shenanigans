package shenanigans.game.network

import shenanigans.engine.ecs.Component
import shenanigans.engine.net.ClientOnly

@ClientOnly
class Synchronized : Component {
    var registration : RegistrationStatus = RegistrationStatus.Disconnected
}

enum class RegistrationStatus {
    Disconnected,
    Sent,
    Registered
}