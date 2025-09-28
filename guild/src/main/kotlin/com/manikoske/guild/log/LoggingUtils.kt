package com.manikoske.guild.log

import com.manikoske.guild.action.Action
import com.manikoske.guild.rules.DifficultyClass
import com.manikoske.guild.character.Effect
import com.manikoske.guild.rules.Event
import com.manikoske.guild.rules.Roll
import com.manikoske.guild.character.CharacterState
import com.manikoske.guild.character.Status
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

    private fun getEffectSymbol(effect: Effect.TargetabilityAlteringEffect): String {
        return blue(SYMBOL_INVISIBLE)
    }

    private fun getEffectSymbol(effect: Effect.ActionMovementAlteringEffect): String {
        return when(effect) {
            is Effect.ActionMovementAlteringEffect.ActionMovementAmountAlteringEffect -> yellow(SYMBOL_ENTANGLED)
            is Effect.ActionMovementAlteringEffect.ActionMovementRestrictingEffect -> purple(SYMBOL_HELD)
        }
    }

    private fun getEffectSymbol(effect: Effect.ActionAvailabilityAlteringEffect): String {
        return when(effect) {
            is Effect.ActionAvailabilityAlteringEffect.NoActionForcingEffect -> red(SYMBOL_STUN)
            is Effect.ActionAvailabilityAlteringEffect.ActionRestrictingEffect -> yellow(SYMBOL_STUN)
            is Effect.ActionAvailabilityAlteringEffect.ActionsForcingEffect -> yellow(SYMBOL_PRONE)
        }
    }

    private fun getEffectSymbol(effect: Effect.HpAffectingOverTimeEffect): String {
        return when(effect) {
            is Effect.HpAffectingOverTimeEffect.DamageOverTimeEffect -> purple(SYMBOL_POISON)
            is Effect.HpAffectingOverTimeEffect.HealingOverTimeEffect -> green(SYMBOL_REGENERATION)
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
            is Roll.DamageOverTimeRoll -> {}
            is Roll.HealOverTimeRoll -> {}
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

    private fun formatStatuses(statuses: List<Status>) : String {
        val formated = statuses.fold ("") { display, status -> display + status.name.name }
        return if (formated.isEmpty()) "" else "<$formated>"
    }

    private fun formatStatus(status: Status) : String {
        val builder = StringBuilder()
        builder.append(status.name.name)
        builder.append("<")
        if (status.actionAvailabilityAlteringEffect != null) {
            builder.append(" ${getEffectSymbol(status.actionAvailabilityAlteringEffect)}")
        }
        if (status.actionMovementAlteringEffect != null) {
            builder.append(" ${getEffectSymbol(status.actionMovementAlteringEffect)}")
        }
        if (status.targetabilityAlteringEffect != null) {
            builder.append(" ${getEffectSymbol(status.targetabilityAlteringEffect)}")
        }
        if (status.hpAffectingOverTimeEffect != null) {
            builder.append(" ${getEffectSymbol(status.hpAffectingOverTimeEffect)}")
        }
        builder.append(">")

        if (status.duration is Status.Duration.RoundLimited) {
            builder.append(" [${status.duration.roundsLeft} rounds left]")
        }
        if (status.hpAffectingOverTimeEffect is Effect.HpAffectingOverTimeEffect.DamageOverTimeEffect) {
            builder.append(" [${formatDice(status.hpAffectingOverTimeEffect.damageDice)} damage]")
        }
        if (status.hpAffectingOverTimeEffect is Effect.HpAffectingOverTimeEffect.HealingOverTimeEffect) {
            builder.append(" [${formatDice(status.hpAffectingOverTimeEffect.healDice)} healing]")
        }

        return builder.toString()
    }

    fun formatCharacterState(characterState: CharacterState) : String {
        val name = characterState.character.bio.name
        val group = characterState.allegiance.name
        val position = characterState.positionNodeId
        val health = formatHitPoints(characterState.currentHitPoints(), characterState.character.maxHitPoints())
        val resources = formatResources(characterState.currentResources(), characterState.character.maxResources())
        val statuses = formatStatuses(characterState.statuses)

        return "${bold(name)} <$group> @$position [$health | $resources] $statuses"
    }

    private fun buildAddStatusResult(builder: StringBuilder, linePrefix: String, result: CharacterState.Result.AddStatusResult) {
        when (result) {
            is CharacterState.Result.AddStatusResult.Added ->
                builder.appendLine(linePrefix + "Gains status ${formatStatus(result.addedStatus)}")
            is CharacterState.Result.AddStatusResult.Replaced ->
                builder.appendLine(linePrefix + "Gains status ${formatStatus(result.addedStatus)} while replacing ${formatStatus(result.replacedStatus)}")
            is CharacterState.Result.AddStatusResult.NothingAdded -> Unit
        }
    }

    private fun buildBoostResourcesResult(builder: StringBuilder, linePrefix: String, result: CharacterState.Result.BoostResourcesResult) {
        when (result) {
            is CharacterState.Result.BoostResourcesResult.AlreadyFull ->
                builder.appendLine(linePrefix + "Does not receive resources boost as already full")
            is CharacterState.Result.BoostResourcesResult.NoResourceBoost ->
                builder.appendLine(linePrefix + "Does not receive any resources boost")
            is CharacterState.Result.BoostResourcesResult.ResourcesBoosted -> {
                builder.appendLine(linePrefix + "${result.amountBoosted} resources boosted")
                if (result.overBoosted > 0) {
                    builder.appendLine(linePrefix + "\t Overboost by ${result.overBoosted}")
                }
            }


        }
    }

    private fun buildTakeDamageResult(builder: StringBuilder, linePrefix: String, result: CharacterState.Result.TakeDamageResult) {
        when (result) {
            is CharacterState.Result.TakeDamageResult.AlreadyDown ->
                builder.appendLine(linePrefix + "Does not take any damage as already down")
            is CharacterState.Result.TakeDamageResult.Downed -> {
                builder.appendLine(linePrefix + "Takes ${result.takenDamage} damage and goes down")
                if (result.damagedOver > 0)
                    builder.appendLine(linePrefix + "\tOverkill by ${result.damagedOver}.")
                if (result.statusesRemovedOnDamage.isNotEmpty()) {
                    builder.appendLine(linePrefix + "\tRemoved statuses: ${formatStatuses(result.statusesRemovedOnDamage)}")
                }
            }
            is CharacterState.Result.TakeDamageResult.NoDamageTaken ->
                builder.appendLine(linePrefix + "Does not take any damage")
            is CharacterState.Result.TakeDamageResult.StillStanding -> {
                builder.appendLine(linePrefix + "Takes ${result.takenDamage} but is still standing")
                if (result.statusesRemovedOnDamage.isNotEmpty()) {
                    builder.appendLine(linePrefix + "\tRemoved statuses: ${formatStatuses(result.statusesRemovedOnDamage)}")
                }
            }
        }
    }

    private fun buildReceiveHealingResult(builder: StringBuilder, linePrefix: String, result: CharacterState.Result.ReceiveHealingResult) {
        when (result) {
            is CharacterState.Result.ReceiveHealingResult.AlreadyFull ->
                builder.appendLine(linePrefix + "Does not receive any healing as already full")
            is CharacterState.Result.ReceiveHealingResult.Healed -> {
                builder.appendLine(linePrefix + "Healed for ${result.amountHealed}")
                if (result.overHealed > 0) {
                    builder.appendLine(linePrefix + "\tOverhealed by ${result.overHealed}")
                }
            }
            is CharacterState.Result.ReceiveHealingResult.NoHeal ->
                builder.appendLine(linePrefix + "Does not receive any healing")
        }
    }

    private fun buildMovementResult(builder: StringBuilder, linePrefix: String, result: CharacterState.Result.MovementResult) {
        when (result) {
            is CharacterState.Result.MovementResult.Movement -> {
                if (result.statusesRemovedByMovement.isNotEmpty()) {
                    builder.appendLine(linePrefix + "Moves to ${result.newPositionNodeIde} and loses: ${formatStatuses(result.statusesRemovedByMovement)}")
                } else {
                    builder.appendLine(linePrefix + "Moves to ${result.newPositionNodeIde}")
                }
            }
            is CharacterState.Result.MovementResult.NoMovement ->
                builder.appendLine(linePrefix + "Does not move")
        }
    }

    private fun buildRemoveStatusesResult(builder: StringBuilder, linePrefix: String, result: CharacterState.Result.RemoveStatusesResult) {
        when (result) {
            is CharacterState.Result.RemoveStatusesResult.NothingRemoved ->
                builder.appendLine(linePrefix + "No statuses were removed")
            is CharacterState.Result.RemoveStatusesResult.Removed ->
                builder.appendLine(linePrefix + "Removed statuses: ${formatStatuses(result.removedStatuses)}")
        }
    }

    private fun buildSpendResourcesResult(builder: StringBuilder, linePrefix: String, result: CharacterState.Result.SpendResourcesResult) {
        when (result) {
            is CharacterState.Result.SpendResourcesResult.InsufficientResources ->
                builder.appendLine(linePrefix + "Does not have enough resources!")
            is CharacterState.Result.SpendResourcesResult.NoResourcesSpent ->
                builder.appendLine(linePrefix + "Does not spend any resources")
            is CharacterState.Result.SpendResourcesResult.ResourcesSpent ->
                builder.appendLine(linePrefix + "Spends ${result.spentAmount} resources. ${result.resourcesRemaining} left")
        }
    }

    private fun buildTickStatusesResult(builder: StringBuilder, linePrefix: String, result: CharacterState.Result.TickStatusesResult) {
        if (result.updatedStatuses.isNotEmpty()) {
            builder.appendLine(linePrefix + "Tick updated statuses: ${formatStatuses(result.updatedStatuses)}")
        }
        if (result.removedStatuses.isNotEmpty()) {
            builder.appendLine(linePrefix + "Tick removed statuses: ${formatStatuses(result.removedStatuses)}")
        }
    }



    private fun formatActionStarted(actionStarted: Event.ActionStarted) : String {
        val builder = StringBuilder()
        builder.appendLine("▶\uFE0F ${formatCharacterState(actionStarted.target)}")
        builder.appendLine("\tExecutes: ${actionStarted.actionName}")

        buildMovementResult(builder,"\t", actionStarted.movementResult)
        buildSpendResourcesResult(builder,"\t", actionStarted.spendResourcesResult)

        return builder.toString()
    }

    private fun formatActionEnded(actionEnded: Event.ActionEnded) : String {
        val builder = StringBuilder()
        builder.appendLine("⏳ ${formatCharacterState(actionEnded.updatedTarget)}")

        if (actionEnded.healOverTimeRolls.isNotEmpty()) {
            builder.appendLine("\t Heal effects: ")
            actionEnded.healOverTimeRolls.forEach { it -> builder.appendLine("\t\t ${formatRoll(it)}") }
            buildReceiveHealingResult(builder, "", actionEnded.receiveHealingResult)
        }
        if (actionEnded.damageOverTimeRolls.isNotEmpty()) {
            builder.appendLine("\t Damage effects: ")
            actionEnded.damageOverTimeRolls.forEach { it -> builder.appendLine("\t\t ${formatRoll(it)}") }
            buildTakeDamageResult(builder, "", actionEnded.takeDamageResult)
        }

        buildTickStatusesResult(builder, "",  actionEnded.tickStatusesResult)

        return builder.toString()
    }

    private fun formatResolutionEvent(event: Event.ResolutionEvent) : String {
        val builder = StringBuilder()
        builder.appendLine(formatCharacterState(event.target))

        when (event) {
            is Event.StatusAdded -> {
                buildAddStatusResult(builder,"\t\t", event.addStatusResult)
            }
            is Event.StatusesRemoved -> {
                buildRemoveStatusesResult(builder,"\t\t", event.removeStatusesResult)
            }
            is Event.Healed -> {
                builder.appendLine("\t\tHealed: ${formatRoll(event.healRoll)}")
                buildReceiveHealingResult(builder,"\t\t", event.receiveHealingResult)
            }
            is Event.ResourceBoosted ->
                buildBoostResourcesResult(builder,"\t\t", event.boostResourcesResult)
            is Event.SpellAttackHit -> {
                builder.appendLine("\t\tSpell attack hit:")
                builder.appendLine("\t\t\tSpell difficulty class: ${formatDifficultyClass(event.spellAttackDifficultyClass)}")
                builder.appendLine("\t\t\tDefense roll: ${formatRoll(event.spellDefenseRoll)}")
                builder.appendLine("\t\t\tDamage roll: ${formatRoll(event.spellDamageRoll)}")
                buildTakeDamageResult(builder,"\t\t\t", event.takeDamageResult)
                buildAddStatusResult(builder,"\t\t\t", event.addStatusResult)
            }
            is Event.SpellAttackMissed -> {
                builder.appendLine("\t\tSpell attack missed:")
                builder.appendLine("\t\t\tSpell difficulty class: ${formatDifficultyClass(event.spellAttackDifficultyClass)}")
                builder.appendLine("\t\t\tDefense roll: ${formatRoll(event.spellDefenseRoll)}")
            }
            is Event.WeaponAttackHit -> {
                builder.appendLine("\t\tWeapon attack hit:")
                builder.appendLine("\t\t\tAttack roll: ${formatRoll(event.weaponAttackRoll)}")
                builder.appendLine("\t\t\tArmor class: ${formatDifficultyClass(event.armorClass)}")
                builder.appendLine("\t\t\tDamage roll: ${formatRoll(event.weaponDamageRoll)}")
                buildTakeDamageResult(builder,"\t\t\t", event.takeDamageResult)
                buildAddStatusResult(builder,"\t\t\t", event.addStatusResult)
            }
            is Event.WeaponAttackMissed -> {
                builder.appendLine("\t\tWeapon attack missed:")
                builder.appendLine("\t\t\t Attack roll: ${formatRoll(event.weaponAttackRoll)}")
                builder.appendLine("\t\t\t Armor class: ${formatDifficultyClass(event.armorClass)}")
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