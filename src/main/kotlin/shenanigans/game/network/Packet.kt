package shenanigans.game.network

import shenanigans.engine.events.Event

abstract class Packet (protected val serverTimeMillis: Int) : Event