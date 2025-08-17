package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.character.CharacterState

data class Turn(
    val takerId : Int,
    val characterStates: List<CharacterState>
) {

    fun simulate(battleground: Battleground): State {

        val taker = characterStates.first {it.character.id == takerId}
        val pointOfView = PointOfView(taker = taker, others = characterStates.filter { it.character.id != takerId })

        val allExecutableActions = taker.allExecutableActions()
        val allVantageNodes = pointOfView.allVantageNodes(battleground = battleground)

        val possibleOutcomes: MutableList<() -> Action.Outcome> = mutableListOf()

        allExecutableActions.forEach { executableAction ->
            val actionMovement = taker.actualMovement(executableAction.movement)
            allVantageNodes
                .filter { vantageNode -> vantageNode.canBeAccessedWith(actionMovement) }
                .forEach { accessibleVantageNode ->
                    when (executableAction) {

                        is Action.SelfAction -> possibleOutcomes.add {
                            executableAction.execute(
                                executor = taker,
                                newPositionNodeId = accessibleVantageNode.nodeId
                            )
                        }
                        is Action.TargetedAction ->  {
                            accessibleVantageNode.targets
                                .filter { target -> executableAction.canTarget(taker, target) }
                                .forEach { validTarget ->
                                    possibleOutcomes.add {
                                        executableAction.execute(
                                            executor = taker,
                                            target = validTarget,
                                            newPositionNodeId = accessibleVantageNode.nodeId
                                        )
                                    }
                                }
                        }
                    }
                }
        }

        val best = possibleOutcomes.maxBy { pointOfView.updateWith(it.invoke()).utility() }.invoke()

        return State(
            updatedCharacterStates = pointOfView.updateWith(best).characterStates(),
            outcome = best,
        )
    }

    data class State(
        val updatedCharacterStates: List<CharacterState>,
        val outcome: Action.Outcome,
    )

}
