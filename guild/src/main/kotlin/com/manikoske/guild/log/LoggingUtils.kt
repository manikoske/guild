package com.manikoske.guild.log

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Effect
import com.manikoske.guild.action.Event
import com.manikoske.guild.character.Character
import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.encounter.Encounter
import com.manikoske.guild.encounter.Round
import com.manikoske.guild.encounter.Turn

/**
 * Utility object for ANSI color formatting in terminal output
 */
object LoggingUtils {
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
    private fun colorize(text: String, color: String): String {
        return "$color$text$RESET"
    }

    // Helper methods for common color formatting
    private fun bold(text: String): String = colorize(text, BOLD)
    private fun italic(text: String): String = colorize(text, ITALIC)
    private fun red(text: String): String = colorize(text, RED)
    private fun green(text: String): String = colorize(text, GREEN)
    private fun yellow(text: String): String = colorize(text, YELLOW)
    private fun blue(text: String): String = colorize(text, BLUE)
    private fun purple(text: String): String = colorize(text, PURPLE)
    private fun cyan(text: String): String = colorize(text, CYAN)
    private fun white(text: String): String = colorize(text, WHITE)
    private fun bgRed(text: String): String = colorize(text, BG_RED)
    private fun bgGreen(text: String): String = colorize(text, BG_GREEN)
    private fun bgBlue(text: String): String = colorize(text, BG_BLUE)
    private fun header(text: String): String = colorize(text, "$BOLD$BG_BLACK$WHITE")

    /**
     * Formats a modifier value with a + sign for positive values
     */
    private fun formatModifier(value: Int): String = if (value >= 0) "+$value" else "$value"

    /**
     * Formats hit points with appropriate color based on percentage of max
     */
    private fun formatHitPoints(current: Int, max: Int): String {
        val percentage = (current.toDouble() / max.toDouble()) * 100
        return when {
            percentage <= 25 -> bgRed(bold("$current/$max"))
            percentage <= 50 -> red("$current/$max")
            percentage <= 75 -> yellow("$current/$max")
            else -> green("$current/$max")
        }
    }

    /**
     * Formats resources with appropriate color based on percentage of max
     */
    private fun formatResources(current: Int, max: Int): String {
        val percentage = (current.toDouble() / max.toDouble()) * 100
        return when {
            percentage <= 25 -> red("$current/$max")
            percentage <= 50 -> yellow("$current/$max")
            else -> blue("$current/$max")
        }
    }

    private fun getEffectSymbol(effect: Effect): String {
        return when(effect) {
            is Effect.ActionForcingEffect.Dying -> red(SYMBOL_DYING)
            is Effect.ActionForcingEffect.Stun -> yellow(SYMBOL_STUN)
            is Effect.ActionForcingEffect.Prone -> yellow(SYMBOL_PRONE)
            is Effect.DamageOverTimeEffect.Bleed -> red(SYMBOL_BLEED)
            is Effect.DamageOverTimeEffect.Poison -> green(SYMBOL_POISON)
            is Effect.HealOverTimeEffect.Regeneration -> green(SYMBOL_REGENERATION)
            is Effect.MovementAlteringEffect.Slow -> cyan(SYMBOL_SLOW)
            is Effect.MovementAlteringEffect.Haste -> cyan(SYMBOL_HASTE)
            is Effect.MovementRestrictingEffect.Entangled -> purple(SYMBOL_ENTANGLED)
            is Effect.MovementRestrictingEffect.Held -> purple(SYMBOL_HELD)
            is Effect.ActionRestrictingEffect.Disarmed -> yellow(SYMBOL_DISARMED)
            is Effect.ActionRestrictingEffect.Silenced -> yellow(SYMBOL_SILENCED)
        }
    }

    private fun logCharacterState(characterState: CharacterState) : String {
        val name = characterState.character.bio.name
        val group = characterState.allegiance.name
        val position = characterState.positionNodeId
        val health = formatHitPoints(characterState.currentHitPoints(), characterState.character.maxHitPoints())
        val resources = formatResources(characterState.currentResources(), characterState.character.maxResources())
        val effects = characterState.effects.all().fold ("") { display, effect -> display + getEffectSymbol(effect) }

        return "${bold(name)} <$group> @$position [$health | $resources] ${if (effects.isNotEmpty()) "⟪$effects⟫" else ""}"
    }

    private fun logCharacter(character: Character) : String {
        return "${character.bio.name}"
    }

    private fun logActionStarted(actionStarted: Event.ActionStarted) : String {
        val builder = StringBuilder()
        builder.append("⚔\uFE0F Executes: ${actionStarted.actionName}.")
        if (actionStarted.newPositionNodeId != actionStarted.target.positionNodeId) {
            builder.append(" Moves to: $actionStarted.newPositionNodeId.")
        }
        if (actionStarted.resourcesSpent > 0) {
            builder.append(" Spends: ${actionStarted.resourcesSpent} resources.")
        }
        return builder.toString()
    }

    private fun logResolutionEvent(resolutionEvent: Event.ResolutionEvent) : String {
        val builder = StringBuilder()
        builder.appendLine()

        // todo target

        when (resolutionEvent) {
            is Event.EffectAdded -> TODO()
            is Event.EffectRemoved -> TODO()
            is Event.Healed -> TODO()
            is Event.ResourceBoosted -> TODO()
            is Event.SpellAttackHit -> TODO()
            is Event.SpellAttackMissed -> TODO()
            is Event.WeaponAttackHit -> TODO()
            is Event.WeaponAttackMissed -> TODO()
        }

        // todo updated target

        return builder.toString()
    }

    private fun logTargetEvents(targetEvents: List<Event.ResolutionEvent>) : String {
        val builder = StringBuilder()
        builder.appendLine("Targets:")
        targetEvents.forEach { it ->
            builder.appendLine("${'\t'}\uD83C\uDFAF {${logResolutionEvent(it)}}:")
            builder.appendLine()
        }
        return builder.toString()
    }

    private fun logSelfResolutionEvent(selfResolutionEvent: Event.ResolutionEvent) : String {
        val builder = StringBuilder()
        builder.appendLine("Self:")
        builder.appendLine("${'\t'}\uD83D\uDD30 {${logResolutionEvent(selfResolutionEvent)}}:")
        builder.appendLine()
        return builder.toString()
    }

    private fun logTurn(state: Turn.State) : String {
        val builder = StringBuilder()
        builder.appendLine("▶\uFE0F Turn: ${logCharacterState(state.outcome.executor)}")
        builder.appendLine()
        builder.appendLine(logActionStarted(state.outcome.actionStarted))
        builder.appendLine()

        if (state.outcome is Action.TargetedActionOutcome) {
            logTargetEvents(state.outcome.targetEvents)
        }

        if (state.outcome.selfResolutionEvent != null) {
            logSelfResolutionEvent(state.outcome.selfResolutionEvent!!)
        }

        // todo log end action

        return builder.toString()
    }

    private fun logRound(state: Round.State) : String {
        val builder = StringBuilder()
        return builder.toString()
    }

    fun logEncounter(state: Encounter.State) : String {
        val builder = StringBuilder()
        return builder.toString()
    }


}