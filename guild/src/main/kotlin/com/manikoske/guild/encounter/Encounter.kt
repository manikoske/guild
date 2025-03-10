package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Outcome
import com.manikoske.guild.character.Character
import java.util.logging.Logger

class Encounter(
    private val battleground: Battleground,
) {

    companion object {
        val LOG = Logger.getLogger(Encounter::class.java.name)
    }


    fun simulate(
        attackers: Set<Character>,
        defenders: Set<Character>,
        attackersStartingNodeId: Int,
        defendersStartingNodeId: Int,
    ) : EncounterState {
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
                        return nextTurn.simulate(battleground)
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

            val choices: MutableList<Choice> = mutableListOf()

            allExecutableActions
                .forEach { executableAction ->
                    allVantageNodes
                        .filter { vantageNode -> vantageNode.accessibleBy(executableAction.movement)}
                        .forEach { accessibleVantageNode ->
                            choices.addAll(accessibleVantageNode.targets
                                .map { target ->
                                    Choice(
                                        action = executableAction,
                                        target = target,
                                        newPositionNodeId = accessibleVantageNode.nodeId
                                    )
                                }
                            )
                        }
                }

        }

        data class Choice(
            val action: Action,
            val target: Target,
            val newPositionNodeId: Int
        ) {
            fun resolve(pointOfView : PointOfView) : Ending {
                return action.outcome.resolve(
                    pointOfView = pointOfView,
                    target = target,
                    newPositionNodeId = newPositionNodeId,
                    resourceCost = action.resourceCost
                )
            }
        }

        data class Ending(
            private val pointOfView: PointOfView
        ) {
            fun utility(): Double {
                return pointOfView.allies.sumOf { it.utility() } - pointOfView.enemies.sumOf { it.utility() }
            }

            fun encounterState(): EncounterState {
                return EncounterState.fromView(pointOfView)
            }
        }
    }


}
