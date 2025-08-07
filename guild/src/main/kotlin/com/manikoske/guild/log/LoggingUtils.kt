package com.manikoske.guild.log

import com.manikoske.guild.action.Action
import com.manikoske.guild.rules.DifficultyClass
import com.manikoske.guild.character.Effect
import com.manikoske.guild.rules.Event
import com.manikoske.guild.rules.Roll
import com.manikoske.guild.character.CharacterState
import com.manikoske.guild.encounter.Encounter
import com.manikoske.guild.encounter.Round
import com.manikoske.guild.encounter.Turn
import com.manikoske.guild.rules.Dice

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
    const val SYMBOL_HIDDEN = "⛶"
    const val SYMBOL_INVISIBLE = "∅"
    const val SYMBOL_ETHEREAL = "\uD83D\uDF04"

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
            is Effect.ActionForcingEffect.Downed -> red(SYMBOL_DYING)
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
            is Effect.TargetabilityRestrictingEffect.Ethereal -> blue(SYMBOL_ETHEREAL)
            is Effect.TargetabilityRestrictingEffect.Hidden -> blue(SYMBOL_HIDDEN)
            is Effect.TargetabilityRestrictingEffect.Invisible -> blue(SYMBOL_INVISIBLE)
        }
    }

    private fun formatDice(dice: Dice) : String {
        val builder = StringBuilder()
        builder.append(dice.dice.joinToString(separator = "+") { "d" + it.sides })
        if (dice.modifier > 0) {
            builder.append(" +${dice.modifier}")
        }
        return builder.toString()
    }

    private fun formatModifier(modifier: Int, label: String) : String {
        return if (modifier == 0) {
            ""
        } else if (modifier < 0) {
            "[$modifier $label]"
        } else {
            "[+$modifier $label]"
        }
    }
    private fun formatMultiplier(multiplier: Int, label: String) : String {
        return if (multiplier == 1) {
            ""
        } else {
            "[x$multiplier $label]"
        }
    }

    private fun formatRoll(roll: Roll) : String {
        val builder = StringBuilder()
        builder.append("${roll.result} = \uD83C\uDFB2 ${roll.rolled.result} (${formatDice(roll.rolled.dice)}) ")

        if (roll is Roll.HasLevelModifier) {
            builder.append(formatModifier(roll.levelModifier, "lvl"))
        }
        if (roll is Roll.HasAttributeModifier) {
            builder.append(formatModifier(roll.attributeModifier, "attr"))
        }
        when (roll) {
            is Roll.WeaponAttackRoll -> {
                builder.append(formatModifier(roll.actionAttackModifier, "actn"))
                builder.append(formatModifier(roll.weaponAttackModifier, "wpn"))
            }
            is Roll.WeaponDamageRoll -> {
                builder.append(formatMultiplier(roll.actionDamageMultiplier, "actn"))
            }
            is Roll.DamageOverTimeRoll -> {
                builder.append(formatEffect(roll.effect))
            }
            is Roll.HealOverTimeRoll -> {
                builder.append(formatEffect(roll.effect))
            }
            is Roll.HealRoll -> {}
            is Roll.InitiativeRoll -> {}
            is Roll.SpellDamageRoll -> {}
            is Roll.SpellDefenseRoll -> {}
        }
        return builder.toString()
    }

    private fun formatDifficultyClass(difficultyClass: DifficultyClass) : String {
        val builder = StringBuilder()
        builder.append("${difficultyClass.result} = ${difficultyClass.baseDifficultyClass} ")
        builder.append(formatModifier(difficultyClass.levelModifier, "lvl"))
        builder.append(formatModifier(difficultyClass.attributeModifier, "attr"))
        when (difficultyClass) {
            is DifficultyClass.SpellAttackDifficultyClass -> {}
            is DifficultyClass.ArmorClass -> {
                builder.append(formatModifier(difficultyClass.armsModifier, "arms"))
            }
        }
        return builder.toString()
    }

    private fun formatEffect(effect: Effect) : String {
        val builder = StringBuilder()
        builder.append(getEffectSymbol(effect))
        if (effect is Effect.TimedEffect) {
            builder.append(" [${effect.roundsLeft} rounds left]")
        }
        if (effect is Effect.DamageOverTimeEffect) {
            builder.append(" [${formatDice(effect.damageDice)} damage]")
        }
        if (effect is Effect.HealOverTimeEffect) {
            builder.append(" [${formatDice(effect.healDice)} healing]")
        }

        return builder.toString()
    }

    fun formatCharacterState(characterState: CharacterState) : String {
        val name = characterState.character.bio.name
        val group = characterState.allegiance.name
        val position = characterState.positionNodeId
        val health = formatHitPoints(characterState.currentHitPoints(), characterState.character.maxHitPoints())
        val resources = formatResources(characterState.currentResources(), characterState.character.maxResources())
        val effects = characterState.effects.all().fold ("") { display, effect -> display + getEffectSymbol(effect) }

        return "${bold(name)} <$group> @$position [$health | $resources]${if (effects.isNotEmpty()) " ⟪$effects⟫" else ""}"
    }

    private fun formatActionStarted(actionStarted: Event.ActionStarted) : String {
        val builder = StringBuilder()
        builder.appendLine("▶\uFE0F ${formatCharacterState(actionStarted.target)}")
        builder.appendLine("\tExecutes: ${actionStarted.actionName}")
        if (actionStarted.newPositionNodeId != actionStarted.target.positionNodeId) {
            builder.appendLine("\tMoves to: ${actionStarted.newPositionNodeId}")
        }
        if (actionStarted.resourcesSpent > 0) {
            builder.appendLine("\tSpends: ${actionStarted.resourcesSpent} resources")
        }
        if (actionStarted.effectsRemovedByMovement.isNotEmpty()) {
            builder.appendLine("\t Effects removed by movement: ")
            actionStarted.effectsRemovedByMovement.forEach { it -> builder.appendLine("\t\t ${formatEffect(it)}") }
        }
        return builder.toString()
    }

    private fun formatActionEnded(actionEnded: Event.ActionEnded) : String {
        val builder = StringBuilder()
        builder.appendLine("⏳ ${formatCharacterState(actionEnded.updatedTarget)}")
        if (actionEnded.updatedStatuses.isNotEmpty()) {
            builder.appendLine("\t Effects updated: ")
            actionEnded.updatedStatuses.forEach { it -> builder.appendLine("\t\t ${formatEffect(it)}") }
        }
        if (actionEnded.removedStatuses.isNotEmpty()) {
            builder.appendLine("\t Effects removed: ")
            actionEnded.updatedStatuses.forEach { it -> builder.appendLine("\t\t ${formatEffect(it)}") }
        }
        if (actionEnded.healOverTimeRolls.isNotEmpty()) {
            builder.appendLine("\t Heal effects: ")
            actionEnded.healOverTimeRolls.forEach { it -> builder.appendLine("\t\t ${formatRoll(it)}") }
        }
        if (actionEnded.damageOverTimeRolls.isNotEmpty()) {
            builder.appendLine("\t Damage effects: ")
            actionEnded.damageOverTimeRolls.forEach { it -> builder.appendLine("\t\t ${formatRoll(it)}") }
        }
        return builder.toString()
    }

    private fun formatResolutionEvent(event: Event.ResolutionEvent) : String {
        val builder = StringBuilder()
        builder.appendLine(formatCharacterState(event.target))

        when (event) {
            is Event.StatusAdded -> {
                builder.appendLine("\tEffects added:")
                event.status.forEach { it -> builder.appendLine("\t\t ${formatEffect(it)}") }
            }
            is Event.StatusesRemoved -> {
                builder.appendLine("\tEffects removed:")
                event.statuses.forEach { it -> builder.appendLine("\t\t ${formatEffect(it)}") }
            }
            is Event.Healed ->
                builder.appendLine("\tHealed: ${formatRoll(event.healRoll)}")
            is Event.ResourceBoosted ->
                builder.appendLine("\tResources boosted by: ${event.amount}")
            is Event.SpellAttackHit -> {
                builder.appendLine("\tSpell attack hit:")
                builder.appendLine("\t\t Spell difficulty class: ${formatDifficultyClass(event.spellAttackDifficultyClass)}")
                builder.appendLine("\t\t Defense roll: ${formatRoll(event.spellDefenseRoll)}")
                builder.appendLine("\t\t Damage roll: ${formatRoll(event.spellDamageRoll)}")
                if (event.statusAddedByDamage.isNotEmpty()) {
                    builder.appendLine("\t\t Effects added: ")
                    event.statusAddedByDamage.forEach { it -> builder.appendLine("\t\t\t ${formatEffect(it)}") }
                }
                if (event.statusesRemovedByDamage.isNotEmpty()) {
                    builder.appendLine("\t\t Effects removed: ")
                    event.statusesRemovedByDamage.forEach { it -> builder.appendLine("\t\t\t ${formatEffect(it)}") }
                }
            }
            is Event.SpellAttackMissed -> {
                builder.appendLine("\tSpell attack missed:")
                builder.appendLine("\t\t Spell difficulty class: ${formatDifficultyClass(event.spellAttackDifficultyClass)}")
                builder.appendLine("\t\t Defense roll: ${formatRoll(event.spellDefenseRoll)}")
            }
            is Event.WeaponAttackHit -> {
                builder.appendLine("\tWeapon attack hit:")
                builder.appendLine("\t\t Attack roll: ${formatRoll(event.weaponAttackRoll)}")
                builder.appendLine("\t\t Armor class: ${formatDifficultyClass(event.armorClass)}")
                builder.appendLine("\t\t Damage roll: ${formatRoll(event.weaponDamageRoll)}")
                if (event.statusAddedByDamage.isNotEmpty()) {
                    builder.appendLine("\t\t Effects added: ")
                    event.statusAddedByDamage.forEach { it -> builder.appendLine("\t\t\t ${formatEffect(it)}") }
                }
                if (event.statusesRemovedByDamage.isNotEmpty()) {
                    builder.appendLine("\t\t Effects removed: ")
                    event.statusesRemovedByDamage.forEach { it -> builder.appendLine("\t\t\t ${formatEffect(it)}") }
                }
            }
            is Event.WeaponAttackMissed -> {
                builder.appendLine("\tWeapon attack missed:")
                builder.appendLine("\t\t Attack roll: ${formatRoll(event.weaponAttackRoll)}")
                builder.appendLine("\t\t Armor class: ${formatDifficultyClass(event.armorClass)}")
            }
        }

        builder.appendLine("\t\uD83D\uDCE6 ${formatCharacterState(event.updatedTarget)}")
        return builder.toString()
    }

    private fun formatTargetEvents(targetEvents: List<Event.ResolutionEvent>) : String {
        val builder = StringBuilder()
        builder.appendLine("Targets:")
        targetEvents.forEach { it ->
            builder.appendLine("\t\uD83C\uDFAF ${formatResolutionEvent(it)}")
        }
        return builder.toString()
    }

    private fun formatSelfEvent(selfResolutionEvent: Event.ResolutionEvent) : String {
        val builder = StringBuilder()
        builder.appendLine("Self:")
        builder.appendLine("\t\uD83D\uDD30 ${formatResolutionEvent(selfResolutionEvent)}:")
        return builder.toString()
    }

    private fun formatTurn(state: Turn.State) : String {
        val builder = StringBuilder()
        builder.appendLine("---------------- Next Turn ----------------")
        builder.appendLine(formatActionStarted(state.outcome.actionStarted))

        if (state.outcome is Action.TargetedActionOutcome) {
            builder.appendLine(formatTargetEvents(state.outcome.targetEvents))
        }

        if (state.outcome.selfResolutionEvent != null) {
            builder.appendLine(formatSelfEvent(state.outcome.selfResolutionEvent!!))
        }

        builder.appendLine(formatActionEnded(state.outcome.actionEnded))
        return builder.toString()
    }

    private fun formatRound(state: Round.State) : String {
        val builder = StringBuilder()
        builder.appendLine("---------------- Round ${state.sequence} ----------------")
        builder.appendLine("\uD83D\uDCCB Initiative:")
        state.initiativeRolls.forEach {
            builder.appendLine("\t ${formatCharacterState(it.target)} - ${formatRoll(it.initiativeRoll)}")
        }
        state.turns.forEach {
            builder.appendLine(formatTurn(it))
        }
        return builder.toString()
    }

    fun formatEncounter(state: Encounter.State) : String {
        val builder = StringBuilder()
        state.rounds.forEach {
            builder.appendLine(formatRound(it))
        }
        return builder.toString()
    }


}