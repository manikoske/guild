package com.manikoske.guild.log

/**
 * Utility object for ANSI color formatting in terminal output
 */
object AnsiColorUtil {
    // ANSI color codes
    const val RESET = "\u001B[0m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val PURPLE = "\u001B[35m"
    const val CYAN = "\u001B[36m"
    const val WHITE = "\u001B[37m"
    const val BOLD = "\u001B[1m"
    const val ITALIC = "\u001B[3m"
    const val BG_BLACK = "\u001B[40m"
    const val BG_RED = "\u001B[41m"
    const val BG_GREEN = "\u001B[42m"
    const val BG_BLUE = "\u001B[44m"

    /**
     * Applies color formatting to text if color output is enabled
     */
    fun colorize(text: String, color: String): String {
        return "$color$text$RESET"
    }

    // Helper methods for common color formatting
    fun bold(text: String): String = colorize(text, BOLD)
    fun italic(text: String): String = colorize(text, ITALIC)
    fun red(text: String): String = colorize(text, RED)
    fun green(text: String): String = colorize(text, GREEN)
    fun yellow(text: String): String = colorize(text, YELLOW)
    fun blue(text: String): String = colorize(text, BLUE)
    fun purple(text: String): String = colorize(text, PURPLE)
    fun cyan(text: String): String = colorize(text, CYAN)
    fun white(text: String): String = colorize(text, WHITE)
    fun bgRed(text: String): String = colorize(text, BG_RED)
    fun bgGreen(text: String): String = colorize(text, BG_GREEN)
    fun bgBlue(text: String): String = colorize(text, BG_BLUE)
    fun header(text: String): String = colorize(text, "$BOLD$BG_BLACK$WHITE")

    /**
     * Formats a modifier value with a + sign for positive values
     */
    fun formatModifier(value: Int): String = if (value >= 0) "+$value" else "$value"
}