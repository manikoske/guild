package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Event

data class Turn(
    private val pointOfView: PointOfView
) {

    fun simulate(battleground: Battleground): State {

        val allExecutableActions = pointOfView.taker.allExecutableActions()
        val allVantageNodes = pointOfView.allVantageNodes(battleground = battleground)

        val possibleOutcomes: MutableList<() -> Action.Outcome> = mutableListOf()

        allExecutableActions.forEach { executableAction ->
            allVantageNodes
                .filter { vantageNode -> executableAction.canAccess(pointOfView.taker, vantageNode) }
                .forEach { accessibleVantageNode ->
                    when (executableAction) {
                        is Action.SelfAction -> possibleOutcomes.add {
                            executableAction.execute(
                                executor = pointOfView.taker,
                                newPositionNodeId = accessibleVantageNode.nodeId
                            )
                        }

                        is Action.TargetedAction ->  {
                            accessibleVantageNode.targets
                                .filter { target -> executableAction.canTarget(pointOfView.taker, target) }
                                .forEach { validTarget ->
                                    possibleOutcomes.add {
                                        executableAction.execute(
                                            executor = pointOfView.taker,
                                            target = validTarget,
                                            newPositionNodeId = accessibleVantageNode.nodeId
                                        )
                                    }
                                }
                        }
                    }
                }
        }

        val best = possibleOutcomes.maxBy { it.invoke() }.invoke()

        return State(
            updatedCharacterStates = ,
            taker = pointOfView.taker,
            outcome = best,
            effectsTicked = best.selfEvent.updatedTarget.tickEffects()
        )
    }

    data class State(
        val updatedCharacterStates: List<CharacterState>,
        val taker: CharacterState,
        val outcome: Action.Outcome,
        val effectsTicked: Event.EffectsTicked
    )

}
