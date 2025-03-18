package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.character.Character
import java.util.logging.Logger

class Encounter(
    private val battleground: Battleground,
) {

    companion object {
        val LOG: Logger = Logger.getLogger(Encounter::class.java.name)
    }


    fun simulate(
        attackers: Set<Character>,
        defenders: Set<Character>,
        attackersStartingNodeId: Int,
        defendersStartingNodeId: Int,
    ): EncounterState {
        val firstRound = Round(
            encounterState = EncounterState.initial(
                attackers = attackers,
                defenders = defenders,
                attackersStartingNodeId = attackersStartingNodeId,
                defendersStartingNodeId = defendersStartingNodeId
            )
        )
        return firstRound.simulate(battleground)
    }


    data class Round(
        private val encounterState: EncounterState
    ) {
        fun simulate(
            battleground: Battleground
        ): EncounterState {
            if (encounterState.hasNoWinner()) {
                val nextRound = Round(encounterState = encounterState
                    .rollInitiatives()
                    .fold(encounterState) { updatedEncounterState, takerId ->
                        val nextTurn = Turn(pointOfView = updatedEncounterState.viewFrom(characterId = takerId))
                        nextTurn.simulate(battleground)
                    }
                )
                return nextRound.simulate(battleground)
            } else {
                return encounterState
            }
        }

    }

    data class Turn(
        private val pointOfView: PointOfView
    ) {

        fun simulate(battleground: Battleground): EncounterState {

            val allExecutableActions = pointOfView.taker.allExecutableActions()
            val allVantageNodes = pointOfView.allVantageNodes(battleground = battleground)

            val endings: MutableList<Ending> = mutableListOf()

            allExecutableActions
                .forEach { executableAction ->
                    allVantageNodes
                        .filter { vantageNode -> vantageNode.accessibleBy(executableAction.movement) }
                        .forEach { accessibleVantageNode ->
                            endings.addAll(accessibleVantageNode.targets
                                .filter { target ->
                                    executableAction.outcome.isValidTarget(
                                        pointOfView.taker,
                                        target
                                    )
                                }
                                .map { target ->
                                    Ending(
                                        action = executableAction,
                                        target = target,
                                        newPositionNodeId = accessibleVantageNode.nodeId,
                                        pointOfView = executableAction.outcome.resolve(
                                            pointOfView = pointOfView,
                                            target = target,
                                            newPositionNodeId = accessibleVantageNode.nodeId,
                                            resourceCost = executableAction.resourceCost
                                        )
                                    )
                                }
                            )
                        }
                }
            val chosenEnding = endings.maxBy { it.utility() }
            LOG.info(chosenEnding.print())
            return chosenEnding.encounterState()
        }

        data class Ending(
            val action: Action,
            val target: Target,
            val newPositionNodeId: Int,
            val pointOfView: PointOfView
        ) {

            fun utility(): Double {
                return pointOfView.allies.sumOf { it.utility() } - pointOfView.enemies.sumOf { it.utility() }
            }

            fun encounterState(): EncounterState {
                return EncounterState.fromView(pointOfView)
            }

            fun print(): String {
                return buildString {
                    appendLine("----- Turn Outcome -----")
                    appendLine("Chosen Action: ${action.name}")
                    appendLine("Ending utility: ${utility()}")
                    appendLine("Target: $target")
                    appendLine("New Position Node ID: $newPositionNodeId")
                    appendLine("Updated Taker State:")
                    appendLine(pointOfView.taker.print())
                    appendLine("Updated Allies State:")
                    pointOfView.allies.forEach { appendLine(it.print()) }
                    appendLine("Updated Enemies State:")
                    pointOfView.enemies.forEach { appendLine(it.print()) }
                    appendLine("------------------------")
                }

            }
        }
    }


}
