package shenanigans.engine.term

object Ansi {
    const val BLACK = "\u001b[30m"
    const val RED = "\u001b[31m"
    const val GREEN = "\u001b[32m"
    const val YELLOW = "\u001b[33m"
    const val BLUE = "\u001b[34m"
    const val MAGENTA = "\u001b[35m"
    const val CYAN = "\u001b[36m"
    const val WHITE = "\u001b[37m"

    const val BRIGHT_BLACK = "\u001b[30;1m"
    const val BRIGHT_RED = "\u001b[31;1m"
    const val BRIGHT_GREEN = "\u001b[32;1m"
    const val BRIGHT_YELLOW = "\u001b[33;1m"
    const val BRIGHT_BLUE = "\u001b[34;1m"
    const val BRIGHT_MAGENTA = "\u001b[35;1m"
    const val BRIGHT_CYAN = "\u001b[36;1m"
    const val BRIGHT_WHITE = "\u001b[37;1m"

    const val BACKGROUND_BLACK = "\u001b[40m"
    const val BACKGROUND_RED = "\u001b[41m"
    const val BACKGROUND_GREEN = "\u001b[42m"
    const val BACKGROUND_YELLOW = "\u001b[43m"
    const val BACKGROUND_BLUE = "\u001b[44m"
    const val BACKGROUND_MAGENTA = "\u001b[45m"
    const val BACKGROUND_CYAN = "\u001b[46m"
    const val BACKGROUND_WHITE = "\u001b[47m"

    const val BACKGROUND_BRIGHT_BLACK = "\u001b[40;1m"
    const val BACKGROUND_BRIGHT_RED = "\u001b[41;1m"
    const val BACKGROUND_BRIGHT_GREEN = "\u001b[42;1m"
    const val BACKGROUND_BRIGHT_YELLOW = "\u001b[43;1m"
    const val BACKGROUND_BRIGHT_BLUE = "\u001b[44;1m"
    const val BACKGROUND_BRIGHT_MAGENTA = "\u001b[45;1m"
    const val BACKGROUND_BRIGHT_CYAN = "\u001b[46;1m"
    const val BACKGROUND_BRIGHT_WHITE = "\u001b[47;1m"

    const val BOLD = "\u001b[1m"
    const val UNDERLINE = "\u001b[4m"
    const val REVERSED = "\u001b[7m"
    const val RESET = "\u001b[0m"
}

class Formatting private constructor(val ansi: String) {
    companion object {
        val BLACK = Formatting(Ansi.BLACK)
        val RED = Formatting(Ansi.RED)
        val GREEN = Formatting(Ansi.GREEN)
        val YELLOW = Formatting(Ansi.YELLOW)
        val BLUE = Formatting(Ansi.BLUE)
        val MAGENTA = Formatting(Ansi.MAGENTA)
        val CYAN = Formatting(Ansi.CYAN)
        val WHITE = Formatting(Ansi.WHITE)

        val BRIGHT_BLACK = Formatting(Ansi.BRIGHT_BLACK)
        val BRIGHT_RED = Formatting(Ansi.BRIGHT_RED)
        val BRIGHT_GREEN = Formatting(Ansi.BRIGHT_GREEN)
        val BRIGHT_YELLOW = Formatting(Ansi.BRIGHT_YELLOW)
        val BRIGHT_BLUE = Formatting(Ansi.BRIGHT_BLUE)
        val BRIGHT_MAGENTA = Formatting(Ansi.BRIGHT_MAGENTA)
        val BRIGHT_CYAN = Formatting(Ansi.BRIGHT_CYAN)
        val BRIGHT_WHITE = Formatting(Ansi.BRIGHT_WHITE)

        val BACKGROUND_BLACK = Formatting(Ansi.BACKGROUND_BLACK)
        val BACKGROUND_RED = Formatting(Ansi.BACKGROUND_RED)
        val BACKGROUND_GREEN = Formatting(Ansi.BACKGROUND_GREEN)
        val BACKGROUND_YELLOW = Formatting(Ansi.BACKGROUND_YELLOW)
        val BACKGROUND_BLUE = Formatting(Ansi.BACKGROUND_BLUE)
        val BACKGROUND_MAGENTA = Formatting(Ansi.BACKGROUND_MAGENTA)
        val BACKGROUND_CYAN = Formatting(Ansi.BACKGROUND_CYAN)
        val BACKGROUND_WHITE = Formatting(Ansi.BACKGROUND_WHITE)

        val BACKGROUND_BRIGHT_BLACK = Formatting(Ansi.BACKGROUND_BRIGHT_BLACK)
        val BACKGROUND_BRIGHT_RED = Formatting(Ansi.BACKGROUND_BRIGHT_RED)
        val BACKGROUND_BRIGHT_GREEN = Formatting(Ansi.BACKGROUND_BRIGHT_GREEN)
        val BACKGROUND_BRIGHT_YELLOW = Formatting(Ansi.BACKGROUND_BRIGHT_YELLOW)
        val BACKGROUND_BRIGHT_BLUE = Formatting(Ansi.BACKGROUND_BRIGHT_BLUE)
        val BACKGROUND_BRIGHT_MAGENTA = Formatting(Ansi.BACKGROUND_BRIGHT_MAGENTA)
        val BACKGROUND_BRIGHT_CYAN = Formatting(Ansi.BACKGROUND_BRIGHT_CYAN)
        val BACKGROUND_BRIGHT_WHITE = Formatting(Ansi.BACKGROUND_BRIGHT_WHITE)

        val BOLD = Formatting(Ansi.BOLD)
        val UNDERLINE = Formatting(Ansi.UNDERLINE)
        val REVERSED = Formatting(Ansi.REVERSED)
        // there is no val RESET = Formatting(Ansi.RESET)
    }

    fun combine(vararg others: Formatting): Formatting {
        val builder = StringBuilder()
        builder.append(this.ansi)
        for (other in others) {
            builder.append(other.ansi)
        }
        return Formatting(builder.toString())
    }

    fun format(text: String): String {
        return this.ansi + text + Ansi.RESET
    }
}