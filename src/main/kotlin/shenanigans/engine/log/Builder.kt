package shenanigans.engine.log

class LogEventBuilder {
    var level: LogEvent.Level? = null
    var scope: String? = null
    var fields: MutableMap<String, String> = mutableMapOf()


    internal fun build(): LogEvent {
        return LogEvent(level!!, scope!!, fields)
    }
}

var LogEventBuilder.message: String?
    get() {
        return fields["message"]
    }
    set(value) {
        if (value == null) {
            fields.remove("message")
        } else {
            fields["message"] = value
        }
    }

fun LogEventBuilder.defaultScope() {
    scope = Thread.currentThread().stackTrace[2].className
}

fun log(init: LogEventBuilder.() -> Unit) {
    Logger.log(LogEventBuilder().apply(init).build())
}

fun debug(init: LogEventBuilder.() -> Unit) {
    log {
        level = LogEvent.Level.DEBUG
        init()
    }
}

fun info(init: LogEventBuilder.() -> Unit) {
    log {
        level = LogEvent.Level.INFO
        init()
    }
}

fun warn(init: LogEventBuilder.() -> Unit) {
    log {
        level = LogEvent.Level.WARNING
        init()
    }
}

fun error(init: LogEventBuilder.() -> Unit) {
    log {
        level = LogEvent.Level.ERROR
        init()
    }
}

fun critical(init: LogEventBuilder.() -> Unit) {
    log {
        level = LogEvent.Level.CRITICAL
        init()
    }
}
