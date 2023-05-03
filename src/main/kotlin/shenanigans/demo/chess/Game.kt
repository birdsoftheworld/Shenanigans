package shenanigans.demo.chess

import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.util.Transform
import java.util.*
import kotlin.reflect.KClass


enum class Color {
    WHITE, BLACK
}

enum class PieceType {
    PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
}

data class Piece(val color: Color, val pieceType: PieceType) : Component {
    fun glyph(): String {
        val letter = when (pieceType) {
            PieceType.PAWN -> "P"
            PieceType.ROOK -> "R"
            PieceType.KNIGHT -> "N"
            PieceType.BISHOP -> "B"
            PieceType.QUEEN -> "Q"
            PieceType.KING -> "K"
        }

        return if (color == Color.WHITE) {
            letter
        } else {
            letter.lowercase(Locale.getDefault())
        }
    }
}

class PieceRenderSystem : System {
    override fun executeRender(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        val pieces = query(setOf(Piece::class, Transform::class))

        for (piece in pieces) {
        }
    }
}