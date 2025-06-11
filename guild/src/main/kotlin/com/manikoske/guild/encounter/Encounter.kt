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

        return (1..20).fold(
            EncounterState(
                updatedCharacterStates =
                attackers.map {
                    CharacterState(
                        character = it,
                        positionNodeId = attackersStartingNodeId,
                        allegiance = CharacterState.Allegiance.Attacker,
                        damageTaken = 0,
                        resourcesSpent = 0,
                        effects = CharacterState.noEffects(),
                    )
                }
                        +
                        defenders.map {
                            CharacterState(
                                character = it,
                                positionNodeId = defendersStartingNodeId,
                                allegiance = CharacterState.Allegiance.Defender,
                                damageTaken = 0,
                                resourcesSpent = 0,
                                effects = CharacterState.noEffects(),
                            )
                        },
                rounds = listOf()
            )
        ) { encounterState, roundSequence ->

            val round = Round(sequence = roundSequence, characterStates = encounterState.updatedCharacterStates)
                .simulate(battleground)

            if (round.turns.isEmpty()) {
                return encounterState
            } else {
                return EncounterState(
                    updatedCharacterStates = round.updatedCharacterStates,
                    rounds = encounterState.rounds + round
                )
            }
        }
    }


    data class Round(
        private val sequence: Int,
        private val characterStates: List<CharacterState>
    ) {
        fun simulate(
            battleground: Battleground
        ): RoundState {

            val initiativeRolls =
                characterStates.map { it.rollInitiative() }.sortedByDescending { it.initiativeRoll.initiative }

            return initiativeRolls
                .map { it.updatedTarget }
                .fold(
                    RoundState(
                        sequence = sequence,
                        updatedCharacterStates = characterStates,
                        initiativeRolls = initiativeRolls,
                        turns = listOf()
                    )
                ) { roundState, turnTaker ->
                    if (roundState.hasNoWinner()) {

                        val turn = Turn(roundState.viewFrom(turnTaker.character.id)).simulate(battleground)

                        roundState.copy(
                            updatedCharacterStates = turn.updatedCharacterStates(),
                            turns = roundState.turns + turn
                        )
                    } else {
                        return roundState
                    }
                }
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
