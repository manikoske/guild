package com.manikoske.guild.encounter

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
        private val characterStates: List<CharacterState>
    ) {
        fun simulate(
            battleground: Battleground
        ): RoundState {

            val initiativeRolls = characterStates.map { it.rollInitiative() }.sortedByDescending { it.initiativeRoll.initiative }
            initiativeRolls.map { it.updatedTarget.character.id }.map { viewFrom(it, ) }

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

        fun viewFrom(
            characterId: Int,
            characterStates: List<CharacterState>
        ): PointOfView {
            val taker = characterStates.first { it.character.id == characterId }
            val allies = characterStates.filter { it.character.id != characterId && it.allegiance == taker.allegiance }
            val enemies = characterStates.filter { it.allegiance != taker.allegiance }

            return PointOfView(
                taker = taker,
                allies = allies,
                enemies = enemies
            )
        }

    }

    data class Turn(
        private val pointOfView: PointOfView
    ) {

        fun simulate(battleground: Battleground): TurnState {

            val allExecutableActions = pointOfView.taker.allExecutableActions()
            val allVantageNodes = pointOfView.allVantageNodes(battleground = battleground)

            val possibleTurnStates: MutableList<TurnState> = mutableListOf()

            allExecutableActions.forEach { executableAction ->
                allVantageNodes
                    .filter { vantageNode -> executableAction.canAccess(pointOfView.taker, vantageNode) }
                    .forEach { accessibleVantageNode ->
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

            val best = possibleTurnStates.maxBy { it.utility() }
            return best.action.execute(
                pointOfView = pointOfView,
                target = best.target,
                newPositionNodeId = best.actionTaken.newPositionNodeId
            )
        }
    }
}
