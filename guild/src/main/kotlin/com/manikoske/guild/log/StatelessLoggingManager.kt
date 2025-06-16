package com.manikoske.guild.log

import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.encounter.Encounter
import com.manikoske.guild.encounter.Round
import com.manikoske.guild.encounter.Turn

/**
 * A truly stateless logging manager that generates logs from the final Encounter.State
 * This replaces the previous LoggingManager which logged events as they happened
 */
object StatelessLoggingManager {
    enum class LogLevel {
        MINIMAL,    // Only basic encounter results
        STANDARD,   // Normal combat logging
        DETAILED,   // Detailed combat logging
        ANALYSIS,   // Full battle analysis with statistics
        ALL         // All possible logging
    }

    /**
     * Main entry point for logging an encounter
     * This method takes the final Encounter.State and generates all logs from it
     * All configuration parameters are passed directly to maintain statelessness
     */
    fun logEncounter(
        encounter: Encounter.State, 
        logLevel: LogLevel = LogLevel.STANDARD,
        fileOutput: Boolean = false,
        outputDirectory: String = "logs"
    ) {

        // Configure loggers with the provided parameters
        configureLoggers(logLevel, fileOutput, outputDirectory)

        // Start with a header
        log("\n===== STARTING NEW ENCOUNTER =====\n")

        // Initialize statistics tracking if analysis is enabled
        if (logLevel >= LogLevel.ANALYSIS) {
            BattleReporter.startTracking(encounter.updatedCharacterStates)
        }

        // Log each round in sequence
        encounter.rounds.forEach { round ->
            logRound(round, logLevel)
        }

        // Log the final encounter summary
        if (logLevel >= LogLevel.MINIMAL) {
            when {
                logLevel >= LogLevel.DETAILED -> DetailedCombatLogger.logEncounter(encounter)
                logLevel >= LogLevel.STANDARD -> CombatLogger.logEncounter(encounter)
                else -> logMinimalEncounterResults(encounter)
            }
        }

        // Finalize statistics tracking if analysis is enabled
        if (logLevel >= LogLevel.ANALYSIS) {
            BattleReporter.finishTracking(encounter)
        }
    }

    /**
     * Private helper method to configure all loggers
     */
    private fun configureLoggers(
        logLevel: LogLevel,
        fileOutput: Boolean,
        outputDirectory: String,
    ) {
        // Configure DetailedCombatLogger with verbosity based on log level
        DetailedCombatLogger.configure(
            enabled = logLevel >= LogLevel.DETAILED,
            verbosityLevel = when (logLevel) {
                LogLevel.MINIMAL -> DetailedCombatLogger.VerbosityLevel.MINIMAL
                LogLevel.STANDARD -> DetailedCombatLogger.VerbosityLevel.NORMAL
                LogLevel.DETAILED -> DetailedCombatLogger.VerbosityLevel.NORMAL
                LogLevel.ANALYSIS -> DetailedCombatLogger.VerbosityLevel.VERBOSE
                LogLevel.ALL -> DetailedCombatLogger.VerbosityLevel.VERBOSE
            }
        )

        // Configure BattleReporter
        BattleReporter.configure(
            outputToFile = fileOutput,
            outputDirectory = "$outputDirectory/reports"
        )

    }

    /**
     * Log a single round from the encounter
     */
    private fun logRound(round: Round.State, logLevel: LogLevel) {
        if (logLevel < LogLevel.STANDARD) return

        // Log initiative rolls
        if (logLevel >= LogLevel.STANDARD) {
            if (logLevel >= LogLevel.DETAILED) {
                DetailedCombatLogger.logInitiative(round.initiativeRolls)
            } else {
                CombatLogger.logInitiative(round.initiativeRolls)
            }
        }

        // Log the round header
        if (logLevel >= LogLevel.STANDARD) {
            if (logLevel >= LogLevel.DETAILED) {
                DetailedCombatLogger.logRound(round)
            } else {
                CombatLogger.logRound(round)
            }
        }

        // Track statistics if analysis is enabled
        if (logLevel >= LogLevel.ANALYSIS) {
            BattleReporter.trackRound(round)
        }

        // Log each turn in the round
        round.turns.forEach { turn ->
            logTurn(turn, logLevel)
        }
    }

    /**
     * Log a single turn from a round
     */
    private fun logTurn(turn: Turn.State, logLevel: LogLevel) {
        if (logLevel < LogLevel.STANDARD) return

        if (logLevel >= LogLevel.STANDARD) {
            if (logLevel >= LogLevel.DETAILED) {
                DetailedCombatLogger.logTurn(turn)
            } else {
                CombatLogger.logTurn(turn)
            }
        }
    }

    /**
     * Internal logging function to ensure all output goes through the logging system
     */
    private fun log(message: String) {
        println(message)
    }

    /**
     * Minimal logging of just the encounter results
     */
    private fun logMinimalEncounterResults(encounter: Encounter.State) {
        val attackers = encounter.updatedCharacterStates.filter { it.allegiance == CharacterState.Allegiance.Attacker }
        val defenders = encounter.updatedCharacterStates.filter { it.allegiance == CharacterState.Allegiance.Defender }

        log("\n===== ENCOUNTER RESULTS =====\n")
        log("Rounds completed: ${encounter.rounds.size}")

        // Print outcome
        val attackersAlive = attackers.any { !it.isDying() }
        val defendersAlive = defenders.any { !it.isDying() }

        log("\nOutcome:")
        when {
            attackersAlive && !defendersAlive -> log("Attackers won!")
            !attackersAlive && defendersAlive -> log("Defenders won!")
            else -> log("Battle ended in a draw or timeout.")
        }

        // Print survivors
        log("\nSurvivors:")
        encounter.updatedCharacterStates.filter { !it.isDying() }.forEach { survivor ->
            log("- ${survivor.character.bio.name} (${survivor.allegiance})")
        }
    }
}
