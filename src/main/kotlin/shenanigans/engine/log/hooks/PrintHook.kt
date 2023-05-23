package shenanigans.engine.log.hooks

import shenanigans.engine.log.LogEvent
import java.io.PrintStream

class PrintHook(private val output: PrintStream = System.err) : LoggerHook {
    override fun log(event: LogEvent) {
        output.print("[${event.level}] ${event.scope}:")
        if (event.fields["message"] != null) {
            output.print(" ${event.fields["message"]}")
        }
        event.fields.filter { it.key != "message" }.forEach { output.print("\n ${it.key}=${it.value}") }
    }
}