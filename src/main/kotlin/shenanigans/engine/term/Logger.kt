package shenanigans.engine.term

import java.util.*

class Logger(private val name: String) {

    fun log(text: String) = log(this.name, text)

    fun err(text: String) = err(this.name, text)

    fun warn(text: String) = warn(this.name, text)

    companion object {
        private const val TIME_PATTERN = "%tT"

        private const val PATTERN = "%s [%s] %s"

        private val TIME_FORMAT = Formatting.BRIGHT_BLACK

        private val LOG_FORMAT = Formatting.GREEN
        private val ERR_FORMAT = Formatting.RED
        private val WARN_FORMAT = Formatting.BRIGHT_YELLOW

        private fun printFormatted(format: Formatting, name: String, text: String) {
            val timeString = String.format(TIME_PATTERN, Date())
            val formattedTime = TIME_FORMAT.format(timeString)

            println(
                String.format(
                    PATTERN,
                    formattedTime,
                    name,
                    format.format(
                        text
                    )
                )
            )
        }

        fun log(name: String, text: String) {
            printFormatted(LOG_FORMAT, name, text)
        }

        fun err(name: String, text: String) {
            printFormatted(ERR_FORMAT, name, text)
        }

        fun warn(name: String, text: String) {
            printFormatted(WARN_FORMAT, name, text)
        }
    }
}