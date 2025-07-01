package com.manikoske.guild.log

import com.manikoske.guild.action.Effect

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

    // Effect symbols
    const val SYMBOL_DYING = "☠"
    const val SYMBOL_STUN = "✧"
    const val SYMBOL_PRONE = "↓"
    const val SYMBOL_BLEED = "♥"
    const val SYMBOL_POISON = "☣"
    const val SYMBOL_REGENERATION = "♻"
    const val SYMBOL_SLOW = "⟳"
    const val SYMBOL_HASTE = "⤻"
    const val SYMBOL_ENTANGLED = "⌗"
    const val SYMBOL_HELD = "◉"
    const val SYMBOL_DISARMED = "⚔"
    const val SYMBOL_SILENCED = "♪"

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

    /**
     * Formats hit points with appropriate color based on percentage of max
     */
    fun formatHitPoints(current: Int, max: Int): String {
        val percentage = (current.toDouble() / max.toDouble()) * 100
        return when {
            percentage <= 25 -> bgRed(bold("$current/$max HP"))
            percentage <= 50 -> red("$current/$max HP")
            percentage <= 75 -> yellow("$current/$max HP")
            else -> green("$current/$max HP")
        }
    }

    /**
     * Formats resources with appropriate color based on percentage of max
     */
    fun formatResources(current: Int, max: Int): String {
        val percentage = (current.toDouble() / max.toDouble()) * 100
        return when {
            percentage <= 25 -> red("$current/$max RP")
            percentage <= 50 -> yellow("$current/$max RP")
            else -> blue("$current/$max RP")
        }
    }

    /**
     * Returns colored effect symbol for a given effect category
     */
    fun getEffectSymbol(category: Effect.Category): String {
        return when(category) {
            is Effect.ActionForcingEffect.Category.Dying -> red(SYMBOL_DYING)
            is Effect.ActionForcingEffect.Category.Stun -> yellow(SYMBOL_STUN)
            is Effect.ActionForcingEffect.Category.Prone -> yellow(SYMBOL_PRONE)
            is Effect.DamageOverTimeEffect.Category.Bleed -> red(SYMBOL_BLEED)
            is Effect.DamageOverTimeEffect.Category.Poison -> green(SYMBOL_POISON)
            is Effect.HealOverTimeEffect.Category.Regeneration -> green(SYMBOL_REGENERATION)
            is Effect.MovementAlteringEffect.Category.Slow -> cyan(SYMBOL_SLOW)
            is Effect.MovementAlteringEffect.Category.Haste -> cyan(SYMBOL_HASTE)
            is Effect.MovementRestrictingEffect.Category.Entangled -> purple(SYMBOL_ENTANGLED)
            is Effect.MovementRestrictingEffect.Category.Held -> purple(SYMBOL_HELD)
            is Effect.ActionRestrictingEffect.Category.Disarmed -> yellow(SYMBOL_DISARMED)
            is Effect.ActionRestrictingEffect.Category.Silenced -> yellow(SYMBOL_SILENCED)
            else -> "?"
        }
    }
}