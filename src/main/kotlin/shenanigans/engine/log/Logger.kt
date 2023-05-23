package shenanigans.engine.log

import shenanigans.engine.log.hooks.LoggerHook
import shenanigans.engine.log.hooks.PrintHook

object Logger {
    var hooks: List<LoggerHook> = listOf(PrintHook())

    fun log(event: LogEvent) {
        hooks.forEach { it.log(event) }
    }
}