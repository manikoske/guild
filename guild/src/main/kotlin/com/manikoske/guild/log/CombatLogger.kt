package com.manikoske.guild.log

import com.manikoske.guild.action.Effect
import com.manikoske.guild.action.Event
import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.encounter.Encounter
import com.manikoske.guild.encounter.Round
import com.manikoske.guild.encounter.Target
import com.manikoske.guild.encounter.Turn

/**
 * A logger that formats and prints combat events to the terminal
 */
object CombatLogger {

    // Color formatting methods that delegate to AnsiColorUtil
    private fun bold(text: String): String = AnsiColorUtil.bold(text)
    private fun red(text: String): String = AnsiColorUtil.red(text)
    private fun green(text: String): String = AnsiColorUtil.green(text)
    private fun yellow(text: String): String = AnsiColorUtil.yellow(text)
    private fun blue(text: String): String = AnsiColorUtil.blue(text)
    private fun purple(text: String): String = AnsiColorUtil.purple(text)
    private fun cyan(text: String): String = AnsiColorUtil.cyan(text)
    private fun white(text: String): String = AnsiColorUtil.white(text)

    // Utility methods for common formatting patterns
    private fun italic(text: String): String = "*$text*"

    fun logEncounter(encounter: Encounter.State) {

        val attackers = encounter.updatedCharacterStates.filter { it.allegiance == CharacterState.Allegiance.Attacker }
        val defenders = encounter.updatedCharacterStates.filter { it.allegiance == CharacterState.Allegiance.Defender }

        println("\n${bold("===== ENCOUNTER SUMMARY =====")}")

        // Print the final state of all characters
        println("\n${bold("Final State:")}")
        println("${cyan("Attackers:")} ${summarizeTeam(attackers)}")
        println("${red("Defenders:")} ${summarizeTeam(defenders)}")

        // Print outcome
        val attackersAlive = attackers.any { !it.isDying() }
        val defendersAlive = defenders.any { !it.isDying() }

        println("\n${bold("Outcome:")}")
        println(formatBattleOutcome(attackersAlive, defendersAlive))

        println("\n${bold("===== ROUND DETAILS =====")}")
        // Rounds are logged separately by StatelessLoggingManager
    }

    private fun formatBattleOutcome(attackersAlive: Boolean, defendersAlive: Boolean): String {
        return when {
            attackersAlive && !defendersAlive -> green("Attackers won!")
            !attackersAlive && defendersAlive -> green("Defenders won!")
            else -> yellow("Battle ended in a draw or timeout.")
        }
    }

    private fun summarizeTeam(team: List<CharacterState>): String {
        return team.joinToString(", ") { characterState ->
            formatCharacterSummary(characterState)
        }
    }

    private fun formatCharacterSummary(characterState: CharacterState): String {
        val name = characterState.character.bio.name
        val status = formatCharacterStatus(characterState)
        val effectsText = formatCharacterEffects(characterState.effects.all())

        return "$name $status$effectsText"
    }

    private fun formatCharacterStatus(characterState: CharacterState): String {
        return if (characterState.isDying()) red("[DEAD]") else green("[ALIVE]")
    }

    private fun formatCharacterEffects(effects: List<Effect>): String {
        return if (effects.isNotEmpty()) {
            " - Effects: ${yellow(effects.joinToString { formatEffect(it) })}"
        } else ""
    }

    /**
     * Log initiative rolls separately to ensure they appear before turn logs
     */
    fun logInitiative(initiativeRolls: List<Event.InitiativeRolled>) {

        println("${bold("Initiative order:")}")
        initiativeRolls.forEachIndexed { index, roll ->
            val character = roll.updatedTarget.character
            val initiative = roll.initiativeRoll.initiative
            println("${index + 1}. ${character.bio.name}: ${yellow(initiative.toString())} (rolled ${roll.initiativeRoll.roll.rolled})")
        }
    }

    fun logRound(round: Round.State) {

        println("\n${bold("==== ROUND ${round.sequence} ====")}")

        // Log each turn (initiative is now logged separately)
        round.turns.forEach { logTurn(it) }
    }

    fun logTurn(turn: Turn.State) {

        val actor = turn.actionTaken.updatedTarget.character
        val actionName = turn.action.name
        val targetInfo = formatTargetInfo(turn.target)

        println("\n${bold("${actor.bio.name}'s Turn:")}")
        println("  ${purple(actionName)} → $targetInfo")

        // Log movement
        val fromNode = turn.actionTaken.updatedTarget.positionNodeId
        val toNode = turn.actionTaken.newPositionNodeId
        if (fromNode != toNode) {
            println("  ${blue("Moved")} from node $fromNode to node $toNode")
        }

        // Log outcome events
        if (turn.outcome.selfEvent != null) {
            println("  ${cyan("Self Effect:")} ${formatEvent(turn.outcome.selfEvent)}")
        }

        turn.outcome.targetEvents.forEach { event ->
            println("  ${formatEvent(event)}")
        }

        // Log effects ticked
        if (turn.effectsTicked.removedEffects.isNotEmpty() || turn.effectsTicked.updatedEffects.isNotEmpty()) {
            println("  ${yellow("Effects Ticked:")}")

            if (turn.effectsTicked.removedEffects.isNotEmpty()) {
                println("    ${red("Removed:")} ${turn.effectsTicked.removedEffects.joinToString { formatEffect(it) }}")
            }

            if (turn.effectsTicked.updatedEffects.isNotEmpty()) {
                println("    ${green("Updated:")} ${turn.effectsTicked.updatedEffects.joinToString { formatEffect(it) }}")
            }

            turn.effectsTicked.damageOverTimeRolls.forEach { roll ->
                println("    ${red("DoT (${formatCategory(roll.category)}):")} ${roll.roll.rolled} damage")
            }

            turn.effectsTicked.healOverTimeRolls.forEach { roll ->
                println("    ${green("HoT (${formatCategory(roll.category)}):")} ${roll.roll.rolled} healing")
            }
        }
    }

    private fun formatTargetInfo(target: Target): String {
        return when (target) {
            is Target.Self -> "Self"
            is Target.SingleAlly -> formatSingleTarget("Ally", target.targetedCharacterStates.first(), target.range)
            is Target.SingleEnemy -> formatSingleTarget("Enemy", target.targetedCharacterStates.first(), target.range)
            is Target.DoubleAlly -> formatMultipleTargets("Allies", target.targetedCharacterStates, target.range)
            is Target.DoubleEnemy -> formatMultipleTargets("Enemies", target.targetedCharacterStates, target.range)
            is Target.NodeAlly -> formatNodeTarget("Node Allies", target.range)
            is Target.NodeEnemy -> formatNodeTarget("Node Enemies", target.range)
            is Target.NodeEveryone -> formatNodeTarget("Everyone in node", target.range)
        }
    }

    private fun formatSingleTarget(type: String, target: CharacterState, range: Int): String {
        return "$type ${target.character.bio.name} (range: $range)"
    }

    private fun formatMultipleTargets(type: String, targets: List<CharacterState>, range: Int): String {
        return "$type: ${targets.joinToString { it.character.bio.name }} (range: $range)"
    }

    private fun formatNodeTarget(description: String, range: Int): String {
        return "$description (range: $range)"
    }

    /**
     * Format a category to display format
     */
    private fun formatCategory(category: Effect.Category): String {
        return when (category) {
            is Effect.ActionForcingEffect.Category -> category.toString()
            is Effect.ActionRestrictingEffect.Category -> category.toString()
            is Effect.MovementAlteringEffect.Category -> category.toString()
            is Effect.MovementRestrictingEffect.Category -> category.toString()
            is Effect.DamageOverTimeEffect.Category -> category.toString()
            is Effect.HealOverTimeEffect.Category -> category.toString()
            else -> "Unknown"
        }
    }

    /**
     * Format an effect with appropriate styling
     */
    private fun formatEffect(effect: Effect): String {
        val detailText = EventFormatter.formatEffectDetails(effect)

        return when (effect) {
            is Effect.ActionForcingEffect -> red(detailText)
            is Effect.ActionRestrictingEffect -> yellow(detailText)
            is Effect.MovementAlteringEffect -> blue(detailText)
            is Effect.MovementRestrictingEffect -> purple(detailText)
            is Effect.DamageOverTimeEffect -> red(detailText)
            is Effect.HealOverTimeEffect -> green(detailText)
            else -> detailText // Fallback
        }
    }

    private fun formatEvent(event: Event): String {
        val baseSummary = EventFormatter.formatEventSummary(event)

        // Add color formatting and additional details to the base summary
        return when (event) {
            is Event.WeaponAttackHit -> formatAttackHit(baseSummary, event.weaponAttackRoll.roll.rolled, event.armorClass.armorClass)
            is Event.WeaponAttackMissed -> formatAttackMissed(baseSummary, event.weaponAttackRoll.roll.rolled, event.armorClass.armorClass)
            is Event.SpellAttackHit -> formatSpellHit(baseSummary, event.spellAttackDifficultyClass.attack, event.spellDefenseRoll.roll.rolled)
            is Event.SpellAttackMissed -> formatSpellMissed(baseSummary, event.spellAttackDifficultyClass.attack, event.spellDefenseRoll.roll.rolled)
            is Event.Healed -> formatHealing(baseSummary, event.healRoll.roll.rolled)
            is Event.EffectAdded -> purple(baseSummary)
            is Event.EffectRemoved -> yellow(baseSummary)
            is Event.ResourceBoosted -> blue(baseSummary)
            is Event.ActionTaken, is Event.EffectsTicked, is Event.InitiativeRolled -> baseSummary
        }
    }

    private fun formatAttackHit(summary: String, rolled: Int, armorClass: Int): String {
        return "${red(summary)} (rolled $rolled vs AC $armorClass)"
    }

    private fun formatAttackMissed(summary: String, rolled: Int, armorClass: Int): String {
        return "${yellow(summary)} (rolled $rolled vs AC $armorClass)"
    }

    private fun formatSpellHit(summary: String, dc: Int, rolled: Int): String {
        return "${purple(summary)} (DC $dc vs rolled $rolled)"
    }

    private fun formatSpellMissed(summary: String, dc: Int, rolled: Int): String {
        return "${yellow(summary)} (DC $dc vs rolled $rolled)"
    }

    private fun formatHealing(summary: String, rolled: Int): String {
        return "${green(summary)} (rolled $rolled)"
    }
}
