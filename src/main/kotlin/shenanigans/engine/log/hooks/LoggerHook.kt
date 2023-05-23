package shenanigans.engine.log.hooks

import shenanigans.engine.log.LogEvent

interface LoggerHook {
    fun log(event: LogEvent)
}