package com.manikoske.guild.log

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Effect
import com.manikoske.guild.action.Event
import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.encounter.Encounter
import com.manikoske.guild.encounter.Round
import com.manikoske.guild.encounter.Turn
import com.manikoske.guild.rules.Die

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

    private fun logDice(dice: Die.Dice) : String {
        val builder = StringBuilder()
        builder.append(dice.dice.map { it.sides }.joinToString(separator = "+", prefix = "d"))
        if (dice.modifier > 0) {
            builder.append(" +${dice.modifier}")
        }
        return builder.toString()
    }

    private fun logModifier(modifier: Int, label: String) : String {
        return if (modifier == 0) {
            ""
        } else if (modifier < 0) {
            "[$modifier $label]"
        } else {
            "[+$modifier $label]"
        }
    }
    private fun logMultiplier(multiplier: Int, label: String) : String {
        return if (multiplier == 1) {
            ""
        } else {
            "[x$multiplier $label]"
        }
    }

    private fun logRoll(roll: Event.Roll) : String {
        val builder = StringBuilder()
        builder.append("${roll.result} = \uD83C\uDFB2 ${roll.roll.rolled} (${logDice(roll.roll.dice)}) ")

        if (roll is Event.Roll.HasLevelModifier) {
            builder.append(logModifier(roll.levelModifier, "lvl"))
        }
        if (roll is Event.Roll.HasAttributeModifier) {
            builder.append(logModifier(roll.attributeModifier, "attr"))
        }
        when (roll) {
            is Event.Roll.WeaponAttackRoll -> {
                builder.append(logModifier(roll.actionAttackModifier, "actn"))
                builder.append(logModifier(roll.weaponAttackModifier, "wpn"))
            }
            is Event.Roll.WeaponDamageRoll -> {
                builder.append(logMultiplier(roll.actionDamageMultiplier, "actn"))
            }
            is Event.Roll.DamageOverTimeRoll -> {}
            is Event.Roll.HealOverTimeRoll -> {}
            is Event.Roll.HealRoll -> {}
            is Event.Roll.InitiativeRoll -> {}
            is Event.Roll.SpellDamageRoll -> {}
            is Event.Roll.SpellDefenseRoll -> {}
        }
        return builder.toString()
    }

    private fun logDifficultyClass(difficultyClass: Event.DifficultyClass) : String {
        val builder = StringBuilder()
        builder.append("${difficultyClass.result} = ${difficultyClass.baseDifficultyClass} ")
        builder.append(logModifier(difficultyClass.levelModifier, "lvl"))
        builder.append(logModifier(difficultyClass.attributeModifier, "attr"))
        when (difficultyClass) {
            is Event.DifficultyClass.SpellAttackDifficultyClass -> {}
            is Event.DifficultyClass.ArmorClass -> {}
        }
        return builder.toString()
    }

    private fun logEffect(effect: Effect) : String {
        val builder = StringBuilder()
        builder.append("${effect.category} ${getEffectSymbol(effect)}")
        if (effect is Effect.TimedEffect) {
            builder.append(" [${effect.roundsLeft} rounds left]")
        }
        if (effect is Effect.DamageOverTimeEffect) {
            builder.append(" [${logDice(effect.damageDice)} damage]")
        }
        if (effect is Effect.HealOverTimeEffect) {
            builder.append(" [${logDice(effect.healDice)} healing]")
        }

        return builder.toString()
    }

    private fun logCharacterState(characterState: CharacterState) : String {
        val name = characterState.character.bio.name
        val group = characterState.allegiance.name
        val position = characterState.positionNodeId
        val health = formatHitPoints(characterState.currentHitPoints(), characterState.character.maxHitPoints())
        val resources = formatResources(characterState.currentResources(), characterState.character.maxResources())
        val effects = characterState.effects.all().fold ("") { display, effect -> display + getEffectSymbol(effect) }

        return "${bold(name)} <$group> @$position [$health | $resources]${if (effects.isNotEmpty()) " ⟪$effects⟫" else ""}"
    }

    private fun logActionStarted(actionStarted: Event.ActionStarted) : String {
        val builder = StringBuilder()
        builder.appendLine("▶\uFE0F ${logCharacterState(actionStarted.target)}")
        builder.appendLine("\tExecutes: ${actionStarted.actionName}")
        if (actionStarted.newPositionNodeId != actionStarted.target.positionNodeId) {
            builder.appendLine("\tMoves to: ${actionStarted.newPositionNodeId}")
        }
        if (actionStarted.resourcesSpent > 0) {
            builder.appendLine("\tSpends: ${actionStarted.resourcesSpent} resources")
        }
        return builder.toString()
    }

    private fun logActionEnded(actionEnded: Event.ActionEnded) : String {
        val builder = StringBuilder()
        builder.appendLine("⏳ ${logCharacterState(actionEnded.updatedTarget)}")
        if (actionEnded.updatedEffects.isNotEmpty()) {
            builder.appendLine("\t Effects updated: ")
            actionEnded.updatedEffects.forEach { it -> builder.appendLine("\t\t ${logEffect(it)}") }
        }
        if (actionEnded.removedEffects.isNotEmpty()) {
            builder.appendLine("\t Effects removed: ")
            actionEnded.updatedEffects.forEach { it -> builder.appendLine("\t\t ${logEffect(it)}") }
        }
        if (actionEnded.healOverTimeRolls.isNotEmpty()) {
            builder.appendLine("\t Heal effects: ")
            actionEnded.healOverTimeRolls.forEach { it -> builder.appendLine("\t\t ${logRoll(it)}") }
        }
        if (actionEnded.damageOverTimeRolls.isNotEmpty()) {
            builder.appendLine("\t Damage effects: ")
            actionEnded.damageOverTimeRolls.forEach { it -> builder.appendLine("\t\t ${logRoll(it)}") }
        }
        return builder.toString()
    }

    private fun logResolutionEvent(event: Event.ResolutionEvent) : String {
        val builder = StringBuilder()
        builder.appendLine(logCharacterState(event.target))

        when (event) {
            is Event.EffectAdded ->
                builder.appendLine("\tEffect added: ${logEffect(event.effect)}")
            is Event.EffectRemoved ->
                builder.appendLine("\tEffect removed: ${logEffect(event.effect)}")
            is Event.Healed ->
                builder.appendLine("\tHealed: ${logRoll(event.healRoll)}")
            is Event.ResourceBoosted ->
                builder.appendLine("\tResources boosted by: ${event.amount}")
            is Event.SpellAttackHit -> {
                builder.appendLine("\tSpell attack hit:")
                builder.appendLine("\t\t Spell difficulty class: ${logDifficultyClass(event.spellAttackDifficultyClass)}")
                builder.appendLine("\t\t Defense roll: ${logRoll(event.spellDefenseRoll)}")
                builder.appendLine("\t\t Damage roll: ${logRoll(event.spellDamageRoll)}")
                if (event.effectsAddedByDamage.isNotEmpty()) {
                    builder.appendLine("\t\t Effects added: ")
                    event.effectsAddedByDamage.forEach { it -> builder.appendLine("\t\t\t ${logEffect(it)}") }
                }
                if (event.effectsRemovedByDamage.isNotEmpty()) {
                    builder.appendLine("\t\t Effects removed: ")
                    event.effectsRemovedByDamage.forEach { it -> builder.appendLine("\t\t\t ${logEffect(it)}") }
                }
            }
            is Event.SpellAttackMissed -> {
                builder.appendLine("\tSpell attack missed:")
                builder.appendLine("\t\t Spell difficulty class: ${logDifficultyClass(event.spellAttackDifficultyClass)}")
                builder.appendLine("\t\t Defense roll: ${logRoll(event.spellDefenseRoll)}")
            }
            is Event.WeaponAttackHit -> {
                builder.appendLine("\tWeapon attack missed:")
                builder.appendLine("\t\t Attack roll: ${logRoll(event.weaponAttackRoll)}")
                builder.appendLine("\t\t Armor class: ${logDifficultyClass(event.armorClass)}")
                builder.appendLine("\t\t Damage roll: ${logRoll(event.weaponDamageRoll)}")
                if (event.effectsAddedByDamage.isNotEmpty()) {
                    builder.appendLine("\t\t Effects added: ")
                    event.effectsAddedByDamage.forEach { it -> builder.appendLine("\t\t\t ${logEffect(it)}") }
                }
                if (event.effectsRemovedByDamage.isNotEmpty()) {
                    builder.appendLine("\t\t Effects removed: ")
                    event.effectsRemovedByDamage.forEach { it -> builder.appendLine("\t\t\t ${logEffect(it)}") }
                }
            }
            is Event.WeaponAttackMissed -> {
                builder.appendLine("\tWeapon attack missed:")
                builder.appendLine("\t\t Attack roll: ${logRoll(event.weaponAttackRoll)}")
                builder.appendLine("\t\t Armor class: ${logDifficultyClass(event.armorClass)}")
            }
        }

        builder.appendLine("\t\uD83D\uDCE6 ${logCharacterState(event.updatedTarget)}")
        return builder.toString()
    }

    private fun logTargetEvents(targetEvents: List<Event.ResolutionEvent>) : String {
        val builder = StringBuilder()
        builder.appendLine("Targets:")
        targetEvents.forEach { it ->
            builder.appendLine("\t\uD83C\uDFAF ${logResolutionEvent(it)}")
        }
        return builder.toString()
    }

    private fun logSelfResolutionEvent(selfResolutionEvent: Event.ResolutionEvent) : String {
        val builder = StringBuilder()
        builder.appendLine("Self:")
        builder.appendLine("\t\uD83D\uDD30 ${logResolutionEvent(selfResolutionEvent)}:")
        return builder.toString()
    }

    private fun logTurn(state: Turn.State) : String {
        val builder = StringBuilder()
        builder.appendLine("---------------- Next Turn ----------------")
        builder.appendLine(logActionStarted(state.outcome.actionStarted))

        if (state.outcome is Action.TargetedActionOutcome) {
            builder.appendLine(logTargetEvents(state.outcome.targetEvents))
        }

        if (state.outcome.selfResolutionEvent != null) {
            builder.appendLine(logSelfResolutionEvent(state.outcome.selfResolutionEvent!!))
        }

        builder.appendLine(logActionEnded(state.outcome.actionEnded))
        return builder.toString()
    }

    private fun logRound(state: Round.State) : String {
        val builder = StringBuilder()
        builder.appendLine("---------------- Round ${state.sequence} ----------------")
        builder.appendLine("\uD83D\uDCCB Initiative:")
        state.initiativeRolls.forEach {
            builder.appendLine("\t ${logCharacterState(it.target)} - ${logRoll(it.initiativeRoll)}")
        }
        state.turns.forEach {
            builder.appendLine(logTurn(it))
        }
        return builder.toString()
    }

    fun logEncounter(state: Encounter.State) : String {
        val builder = StringBuilder()
        state.rounds.forEach {
            builder.appendLine(logRound(it))
        }
        return builder.toString()
    }


}