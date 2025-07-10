package com.manikoske.guild.log

import com.manikoske.guild.action.Event
import com.manikoske.guild.character.Class
import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.encounter.Encounter
import com.manikoske.guild.encounter.Round
import com.manikoske.guild.encounter.Turn
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * A specialized logger for generating detailed battle statistics and analytics
 */
object BattleReporter {
    private var outputToFile = false
    private var outputDirectory = "battle_reports"

    // Collection of statistics during the battle
    private data class CharacterStatistics(
        val characterId: Int,
        val name: String,
        val characterClass: Class,
        var damageTaken: Int = 0,
        var damageDealt: Int = 0, 
        var healingReceived: Int = 0,
        var healingProvided: Int = 0,
        var attacksAttempted: Int = 0,
        var attacksHit: Int = 0,
        var spellsAttempted: Int = 0,
        var spellsHit: Int = 0,
        var actionsPerformed: Int = 0,
        var effectsApplied: Int = 0,
        var effectsReceived: Int = 0,
        var turnsAlive: Int = 0,
        var resourcesSpent: Int = 0,
        var didSurvive: Boolean = true
    ) {
        val attackHitPercentage: Double get() = if (attacksAttempted > 0) (attacksHit.toDouble() / attacksAttempted) * 100 else 0.0
        val spellHitPercentage: Double get() = if (spellsAttempted > 0) (spellsHit.toDouble() / spellsAttempted) * 100 else 0.0
    }

    private data class TeamStatistics(
        val allegiance: CharacterState.Allegiance,
        var totalDamageDealt: Int = 0,
        var totalDamageTaken: Int = 0,
        var totalHealingProvided: Int = 0,
        var totalHealingReceived: Int = 0,
        var totalAttacksAttempted: Int = 0,
        var totalAttacksHit: Int = 0,
        var totalSpellsAttempted: Int = 0,
        var totalSpellsHit: Int = 0,
        var totalActionsPerformed: Int = 0,
        var totalEffectsApplied: Int = 0,
        var totalEffectsReceived: Int = 0,
        var membersAlive: Int = 0,
        var membersTotal: Int = 0,
        var resourcesSpent: Int = 0
    ) {
        val survivalRate: Double get() = if (membersTotal > 0) (membersAlive.toDouble() / membersTotal) * 100 else 0.0
        val attackHitRate: Double get() = if (totalAttacksAttempted > 0) (totalAttacksHit.toDouble() / totalAttacksAttempted) * 100 else 0.0
        val spellHitRate: Double get() = if (totalSpellsAttempted > 0) (totalSpellsHit.toDouble() / totalSpellsAttempted) * 100 else 0.0
    }

    private data class BattleStatistics(
        val startTime: LocalDateTime = LocalDateTime.now(),
        var endTime: LocalDateTime = LocalDateTime.now(),
        var totalRounds: Int = 0,
        var totalTurns: Int = 0,
        var totalDamageDealt: Int = 0,
        var totalHealingProvided: Int = 0,
        var totalEffectsApplied: Int = 0,
        val characterStats: MutableMap<Int, CharacterStatistics> = mutableMapOf(),
        val teamStats: MutableMap<CharacterState.Allegiance, TeamStatistics> = mutableMapOf()
    ) {
        val battleDuration: Long get() = java.time.Duration.between(startTime, endTime).seconds
        val avgDamagePerTurn: Double get() = if (totalTurns > 0) totalDamageDealt.toDouble() / totalTurns else 0.0
        val avgHealingPerTurn: Double get() = if (totalTurns > 0) totalHealingProvided.toDouble() / totalTurns else 0.0
    }

    private var currentBattleStats = BattleStatistics()
    private var isTracking = false

    fun configure(outputToFile: Boolean = false, outputDirectory: String = "battle_reports") {
        this.outputToFile = outputToFile
        this.outputDirectory = outputDirectory
    }

    fun startTracking(initialCharacterStates: List<CharacterState>) {

        // Initialize a new battle statistics object
        isTracking = true
        currentBattleStats = BattleStatistics()

        // Initialize team statistics
        val attackers = initialCharacterStates.filter { it.allegiance == CharacterState.Allegiance.Attacker }
        val defenders = initialCharacterStates.filter { it.allegiance == CharacterState.Allegiance.Defender }

        currentBattleStats.teamStats.put(
            CharacterState.Allegiance.Attacker,
            TeamStatistics(
                allegiance = CharacterState.Allegiance.Attacker,
                membersTotal = attackers.size,
                membersAlive = attackers.size
            )
        )

        currentBattleStats.teamStats.put(
            CharacterState.Allegiance.Defender,
            TeamStatistics(
                allegiance = CharacterState.Allegiance.Defender,
                membersTotal = defenders.size,
                membersAlive = defenders.size
            )
        )

        // Initialize character statistics
        initialCharacterStates.forEach { charState ->
            currentBattleStats.characterStats.put(
                charState.character.id,
                CharacterStatistics(
                    characterId = charState.character.id,
                    name = charState.character.bio.name,
                    characterClass = charState.character.clazz()
                )
            )
        }
    }

    fun trackRound(round: Round.State) {
        if (!enabled || !isTracking) return

        currentBattleStats.totalRounds = round.sequence
        currentBattleStats.totalTurns += round.turns.size

        // Track character survival after this round
        round.updatedCharacterStates.forEach { charState ->
            val charStats = currentBattleStats.characterStats[charState.character.id] ?: return@forEach
            if (charState.isDying() && charStats.didSurvive) {
                charStats.didSurvive = false
                val teamStats = currentBattleStats.teamStats[charState.allegiance] ?: return@forEach
                teamStats.membersAlive--
            }
        }

        // Track turn-based statistics
        round.turns.forEach { turn ->
            trackTurn(turn)
        }
    }

    private fun trackTurn(turn: Turn.State) {
        if (!enabled || !isTracking) return

        val actorId = turn.actionTaken.updatedTarget.character.id
        val actorAllegiance = turn.actionTaken.updatedTarget.allegiance
        val actorStats = currentBattleStats.characterStats[actorId] ?: return
        val actorTeamStats = currentBattleStats.teamStats[actorAllegiance] ?: return

        // Track action performed
        actorStats.actionsPerformed++
        actorTeamStats.totalActionsPerformed++
        actorStats.turnsAlive++
        actorStats.resourcesSpent += turn.actionTaken.resourceCost
        actorTeamStats.resourcesSpent += turn.actionTaken.resourceCost

        // Track self event
        turn.outcome.selfEvent?.let { event ->
            trackEventStatistics(event, actorId, actorAllegiance)
        }

        // Track target events
        turn.outcome.targetEvents.forEach { event ->
            val targetId = event.updatedTarget.character.id
            val targetAllegiance = event.updatedTarget.allegiance
            trackEventStatistics(event, targetId, targetAllegiance)

            // Track attack success/failure
            when (event) {
                is Event.WeaponAttackHit -> {
                    actorStats.attacksAttempted++
                    actorStats.attacksHit++
                    actorTeamStats.totalAttacksAttempted++
                    actorTeamStats.totalAttacksHit++

                    val damage = event.weaponDamageRoll.damage
                    actorStats.damageDealt += damage
                    actorTeamStats.totalDamageDealt += damage

                    val targetStats = currentBattleStats.characterStats[targetId] ?: return@forEach
                    val targetTeamStats = currentBattleStats.teamStats[targetAllegiance] ?: return@forEach
                    targetStats.damageTaken += damage
                    targetTeamStats.totalDamageTaken += damage

                    // Track effects
                    if (event.effectsAddedByDamage.isNotEmpty()) {
                        actorStats.effectsApplied += event.effectsAddedByDamage.size
                        actorTeamStats.totalEffectsApplied += event.effectsAddedByDamage.size
                        targetStats.effectsReceived += event.effectsAddedByDamage.size
                        targetTeamStats.totalEffectsReceived += event.effectsAddedByDamage.size
                    }
                }
                is Event.WeaponAttackMissed -> {
                    actorStats.attacksAttempted++
                    actorTeamStats.totalAttacksAttempted++
                }
                is Event.SpellAttackHit -> {
                    actorStats.spellsAttempted++
                    actorStats.spellsHit++
                    actorTeamStats.totalSpellsAttempted++
                    actorTeamStats.totalSpellsHit++

                    val damage = event.spellDamageRoll.damage
                    actorStats.damageDealt += damage
                    actorTeamStats.totalDamageDealt += damage

                    val targetStats = currentBattleStats.characterStats[targetId] ?: return@forEach
                    val targetTeamStats = currentBattleStats.teamStats[targetAllegiance] ?: return@forEach
                    targetStats.damageTaken += damage
                    targetTeamStats.totalDamageTaken += damage

                    // Track effects
                    if (event.effectsAddedByDamage.isNotEmpty()) {
                        actorStats.effectsApplied += event.effectsAddedByDamage.size
                        actorTeamStats.totalEffectsApplied += event.effectsAddedByDamage.size
                        targetStats.effectsReceived += event.effectsAddedByDamage.size
                        targetTeamStats.totalEffectsReceived += event.effectsAddedByDamage.size
                    }
                }
                is Event.SpellAttackMissed -> {
                    actorStats.spellsAttempted++
                    actorTeamStats.totalSpellsAttempted++
                }
                is Event.Healed -> {
                    val healing = event.healRoll.heal
                    actorStats.healingProvided += healing
                    actorTeamStats.totalHealingProvided += healing

                    val targetStats = currentBattleStats.characterStats[targetId] ?: return@forEach
                    val targetTeamStats = currentBattleStats.teamStats[targetAllegiance] ?: return@forEach
                    targetStats.healingReceived += healing
                    targetTeamStats.totalHealingReceived += healing
                }
                is Event.EffectAdded -> {
                    actorStats.effectsApplied++
                    actorTeamStats.totalEffectsApplied++

                    val targetStats = currentBattleStats.characterStats[targetId] ?: return@forEach
                    val targetTeamStats = currentBattleStats.teamStats[targetAllegiance] ?: return@forEach
                    targetStats.effectsReceived++
                    targetTeamStats.totalEffectsReceived++
                }
                else -> { /* Other event types */ }
            }
        }

        // Track damage over time and healing over time
        val effectsTicked = turn.actionEnded
        val dotDamage = effectsTicked.damageOverTimeRolls.sumOf { it.roll.rolled }
        val hotHealing = effectsTicked.healOverTimeRolls.sumOf { it.roll.rolled }

        if (dotDamage > 0) {
            val targetStats = currentBattleStats.characterStats[actorId] ?: return
            val targetTeamStats = currentBattleStats.teamStats[actorAllegiance] ?: return
            targetStats.damageTaken += dotDamage
            targetTeamStats.totalDamageTaken += dotDamage
            currentBattleStats.totalDamageDealt += dotDamage
        }

        if (hotHealing > 0) {
            val targetStats = currentBattleStats.characterStats[actorId] ?: return
            val targetTeamStats = currentBattleStats.teamStats[actorAllegiance] ?: return
            targetStats.healingReceived += hotHealing
            targetTeamStats.totalHealingReceived += hotHealing
            currentBattleStats.totalHealingProvided += hotHealing
        }
    }

    private fun trackEventStatistics(event: Event, targetId: Int, targetAllegiance: CharacterState.Allegiance) {
        // Update specific statistics based on event type
        // Most of this is handled in the trackTurn method, but we could add more specific tracking here
    }

    fun finishTracking(encounter: Encounter.State) {
        if (!enabled || !isTracking) return

        // Set end time
        currentBattleStats.endTime = LocalDateTime.now()

        // Update final statistics
        currentBattleStats.totalDamageDealt = encounter.rounds.sumOf { round ->
            round.turns.sumOf { turn ->
                // Sum weapon and spell attack damage
                turn.outcome.targetEvents.sumOf { event ->
                    when (event) {
                        is Event.WeaponAttackHit -> event.weaponDamageRoll.damage
                        is Event.SpellAttackHit -> event.spellDamageRoll.damage
                        else -> 0
                    }
                } + 
                // Add DoT damage
                turn.actionEnded.damageOverTimeRolls.sumOf { it.roll.rolled }
            }
        }

        currentBattleStats.totalHealingProvided = encounter.rounds.sumOf { round ->
            round.turns.sumOf { turn ->
                // Sum healing from heal events
                turn.outcome.targetEvents.sumOf { event ->
                    when (event) {
                        is Event.Healed -> event.healRoll.heal
                        else -> 0
                    }
                } + 
                // Add HoT healing
                turn.actionEnded.healOverTimeRolls.sumOf { it.roll.rolled }
            }
        }

        currentBattleStats.totalEffectsApplied = encounter.rounds.sumOf { round ->
            round.turns.sumOf { turn ->
                // Count effects added
                turn.outcome.targetEvents.sumOf { event ->
                    when (event) {
                        is Event.EffectAdded -> 1
                        is Event.WeaponAttackHit -> event.effectsAddedByDamage.size
                        is Event.SpellAttackHit -> event.effectsAddedByDamage.size
                        else -> 0
                    }
                }
            }
        }

        // Print report to console
        printBattleReport()

        // Save report to file if enabled
        if (outputToFile) {
            saveBattleReportToFile()
        }
    }

    private fun printBattleReport() {
        if (!isTracking) return
        val stats = currentBattleStats

        println("\n========== BATTLE STATISTICS REPORT ==========")
        println("Battle Duration: ${stats.battleDuration} seconds")
        println("Total Rounds: ${stats.totalRounds}")
        println("Total Turns: ${stats.totalTurns}")
        println("Total Damage: ${stats.totalDamageDealt} (${String.format("%.1f", stats.avgDamagePerTurn)} per turn)")
        println("Total Healing: ${stats.totalHealingProvided} (${String.format("%.1f", stats.avgHealingPerTurn)} per turn)")
        println("Total Effects Applied: ${stats.totalEffectsApplied}")

        // Print team statistics
        println("\n----- TEAM STATISTICS -----")
        stats.teamStats.values.forEach { teamStats ->
            val teamName = if (teamStats.allegiance == CharacterState.Allegiance.Attacker) "Attackers" else "Defenders"
            println("\n$teamName:")
            println("  Survival: ${teamStats.membersAlive}/${teamStats.membersTotal} (${String.format("%.1f", teamStats.survivalRate)}%)")
            println("  Damage Dealt: ${teamStats.totalDamageDealt}")
            println("  Damage Taken: ${teamStats.totalDamageTaken}")
            println("  Healing Provided: ${teamStats.totalHealingProvided}")
            println("  Healing Received: ${teamStats.totalHealingReceived}")
            println("  Attack Hit Rate: ${String.format("%.1f", teamStats.attackHitRate)}% (${teamStats.totalAttacksHit}/${teamStats.totalAttacksAttempted})")
            println("  Spell Hit Rate: ${String.format("%.1f", teamStats.spellHitRate)}% (${teamStats.totalSpellsHit}/${teamStats.totalSpellsAttempted})")
            println("  Resources Spent: ${teamStats.resourcesSpent}")
        }

        // Print individual character statistics
        println("\n----- CHARACTER STATISTICS -----")
        stats.characterStats.values.sortedBy { it.characterId }.forEach { charStats ->
            val statusText = if (charStats.didSurvive) "SURVIVED" else "DEFEATED"
            println("\n${charStats.name} (${charStats.characterClass}) - $statusText")
            println("  Damage Dealt: ${charStats.damageDealt}")
            println("  Damage Taken: ${charStats.damageTaken}")
            println("  Healing Provided: ${charStats.healingProvided}")
            println("  Healing Received: ${charStats.healingReceived}")
            println("  Attack Hit Rate: ${String.format("%.1f", charStats.attackHitPercentage)}% (${charStats.attacksHit}/${charStats.attacksAttempted})")
            println("  Spell Hit Rate: ${String.format("%.1f", charStats.spellHitPercentage)}% (${charStats.spellsHit}/${charStats.spellsAttempted})")
            println("  Total Actions: ${charStats.actionsPerformed}")
            println("  Effects Applied: ${charStats.effectsApplied}")
            println("  Effects Received: ${charStats.effectsReceived}")
            println("  Turns Alive: ${charStats.turnsAlive}")
            println("  Resources Spent: ${charStats.resourcesSpent}")
        }

        println("\n==========================================")
    }

    private fun saveBattleReportToFile() {
        if (!isTracking) return
        val stats = currentBattleStats

        // Create output directory if it doesn't exist
        val directory = File(outputDirectory)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        // Create a filename with timestamp
        val timestamp = stats.startTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val attackersSize = stats.teamStats[CharacterState.Allegiance.Attacker]?.membersTotal ?: 0
        val defendersSize = stats.teamStats[CharacterState.Allegiance.Defender]?.membersTotal ?: 0
        val filename = "battle_report_${attackersSize}v${defendersSize}_${timestamp}.txt"

        try {
            val file = File(directory, filename)
            file.printWriter().use { out ->
                // Write battle overview
                out.println("BATTLE STATISTICS REPORT")
                out.println("======================")
                out.println("Date: ${stats.startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
                out.println("Duration: ${stats.battleDuration} seconds")
                out.println("Total Rounds: ${stats.totalRounds}")
                out.println("Total Turns: ${stats.totalTurns}")
                out.println("Total Damage: ${stats.totalDamageDealt} (${String.format("%.1f", stats.avgDamagePerTurn)} per turn)")
                out.println("Total Healing: ${stats.totalHealingProvided} (${String.format("%.1f", stats.avgHealingPerTurn)} per turn)")
                out.println("Total Effects Applied: ${stats.totalEffectsApplied}")

                // Write team statistics
                out.println("\nTEAM STATISTICS")
                out.println("==============\n")
                stats.teamStats.values.forEach { teamStats ->
                    val teamName = if (teamStats.allegiance == CharacterState.Allegiance.Attacker) "Attackers" else "Defenders"
                    out.println("$teamName:")
                    out.println("  Survival: ${teamStats.membersAlive}/${teamStats.membersTotal} (${String.format("%.1f", teamStats.survivalRate)}%)")
                    out.println("  Damage Dealt: ${teamStats.totalDamageDealt}")
                    out.println("  Damage Taken: ${teamStats.totalDamageTaken}")
                    out.println("  Healing Provided: ${teamStats.totalHealingProvided}")
                    out.println("  Healing Received: ${teamStats.totalHealingReceived}")
                    out.println("  Attack Hit Rate: ${String.format("%.1f", teamStats.attackHitRate)}% (${teamStats.totalAttacksHit}/${teamStats.totalAttacksAttempted})")
                    out.println("  Spell Hit Rate: ${String.format("%.1f", teamStats.spellHitRate)}% (${teamStats.totalSpellsHit}/${teamStats.totalSpellsAttempted})")
                    out.println("  Resources Spent: ${teamStats.resourcesSpent}")
                    out.println()
                }

                // Write individual character statistics
                out.println("CHARACTER STATISTICS")
                out.println("====================\n")
                stats.characterStats.values.sortedBy { it.characterId }.forEach { charStats ->
                    val statusText = if (charStats.didSurvive) "SURVIVED" else "DEFEATED"
                    out.println("${charStats.name} (${charStats.characterClass}) - $statusText")
                    out.println("  Damage Dealt: ${charStats.damageDealt}")
                    out.println("  Damage Taken: ${charStats.damageTaken}")
                    out.println("  Healing Provided: ${charStats.healingProvided}")
                    out.println("  Healing Received: ${charStats.healingReceived}")
                    out.println("  Attack Hit Rate: ${String.format("%.1f", charStats.attackHitPercentage)}% (${charStats.attacksHit}/${charStats.attacksAttempted})")
                    out.println("  Spell Hit Rate: ${String.format("%.1f", charStats.spellHitPercentage)}% (${charStats.spellsHit}/${charStats.spellsAttempted})")
                    out.println("  Total Actions: ${charStats.actionsPerformed}")
                    out.println("  Effects Applied: ${charStats.effectsApplied}")
                    out.println("  Effects Received: ${charStats.effectsReceived}")
                    out.println("  Turns Alive: ${charStats.turnsAlive}")
                    out.println("  Resources Spent: ${charStats.resourcesSpent}")
                    out.println()
                }
            }
            println("Battle report saved to: ${file.absolutePath}")
        } catch (e: Exception) {
            println("Error saving battle report: ${e.message}")
        }
    }
}
