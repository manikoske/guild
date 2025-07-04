package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Event

data class Turn(
    private val pointOfView: PointOfView
) {

    fun simulate(battleground: Battleground): State {

        val allExecutableActions = pointOfView.taker.allExecutableActions()
        val allVantageNodes = pointOfView.allVantageNodes(battleground = battleground)

        val possibleTurnStates: MutableList<State> = mutableListOf()

        allExecutableActions.forEach { executableAction ->
            allVantageNodes
                .filter { vantageNode -> executableAction.canAccess(pointOfView.taker, vantageNode) }
                .forEach { accessibleVantageNode ->
                    when (executableAction) {
                        is Action.NoOutcomeAction -> TODO()
                        is Action.SelfSupportAction -> TODO()
                        is Action.OutcomeAction -> TODO ()
                    }
                    accessibleVantageNode.targets
                        .filter { target -> executableAction.canTarget(pointOfView.taker, target) }
                        .forEach { validTarget ->
                            possibleTurnStates.add(
                                executableAction.execute(
                                    pointOfView = pointOfView,
                                    target = validTarget,
                                    newPositionNodeId = accessibleVantageNode.nodeId
                                )
                            )
                        }
                }
        }

        val best = possibleTurnStates.maxBy { it.utility }

        val result = best.action.execute(
            pointOfView = pointOfView,
            target = best.target,
            newPositionNodeId = best.actionTaken.newPositionNodeId
        )

        return result
    }

    data class State(
        val updatedCharacterStates: List<CharacterState>,
        val action: Action,
        val target: Target,
        val actionTaken: Event.ActionTaken,
        val outcome: Action.Outcome,
        val effectsTicked: Event.EffectsTicked,
        val utility: Double
    )

}
