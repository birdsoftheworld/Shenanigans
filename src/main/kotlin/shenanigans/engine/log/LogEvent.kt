package shenanigans.engine.log

class LogEvent(val level: Level, val scope: String, val fields: Map<String, String> = emptyMap()) {
    enum class Level(val number: Int) {
        DEBUG(-2),
        INFO(-1),
        WARNING(0),
        ERROR(1),
        CRITICAL(2)
    }
}