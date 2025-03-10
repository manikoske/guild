package com.manikoske.guild.encounter

import com.manikoske.guild.action.Outcome
import com.manikoske.guild.character.Character

class Encounter(
    private val battleground: Battleground,
) {

    fun simulate(
        attackers: Set<Character>,
        defenders: Set<Character>,
        attackersStartingNodeId: Int,
        defendersStartingNodeId: Int,
    ) : EncounterState {
        return Round(
            encounterState = EncounterState.initial(
                attackers = attackers,
                defenders = defenders,
                attackersStartingNodeId = attackersStartingNodeId,
                defendersStartingNodeId = defendersStartingNodeId
            )
        ).simulate(battleground)
    }


    data class Round(
        private val encounterState: EncounterState
    ) {
        fun simulate(
            battleground: Battleground
        ): EncounterState {
            return if (encounterState.hasNoWinner()) {
                Round(
                    encounterState = encounterState.rollInitiatives()
                        .fold(encounterState) { updatedEncounterState, takerId ->
                            Turn(pointOfView = updatedEncounterState.viewFrom(characterId = takerId)).simulate(
                                battleground
                            )
                        }).simulate(battleground)
            } else {
                encounterState
            }
        }

    }

    data class Turn(
        private val pointOfView: PointOfView
    ) {

        fun simulate(battleground: Battleground): EncounterState {

            val eventualEndings: MutableList<Ending> = mutableListOf()

            pointOfView.taker.allExecutableActions().forEach { executableAction ->
                pointOfView.allVantageNodes(battleground = battleground).filter { vantageNode ->
                    vantageNode.accessibleBy(executableAction.movement)
                }.forEach { accessibleVantageNode ->
                    eventualEndings.addAll(
                        accessibleVantageNode.targets
                            .map { target ->
                                executableAction.outcome.resolve(
                                    pointOfView = pointOfView,
                                    target = target,
                                    newPositionNodeId = accessibleVantageNode.nodeId,
                                    resourceCost = executableAction.resourceCost
                                )
                            }
                            .filterIsInstance<Outcome.OutcomeResult.AppliedToTarget>()
                            .map { appliedToTarget -> Ending(pointOfView = appliedToTarget.updatedPointOfView) }
                    )
                }
            }
            return eventualEndings.maxBy { it.utility() }.encounterState()
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
