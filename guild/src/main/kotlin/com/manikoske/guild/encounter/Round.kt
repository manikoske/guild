package com.manikoske.guild.encounter

import com.manikoske.guild.character.CharacterState
import com.manikoske.guild.rules.Event
import com.manikoske.guild.rules.Rules

data class Round(
    private val sequence: Int,
    private val characterStates: List<CharacterState>
) {
    fun simulate(
        battleground: Battleground
    ): State {

        val initiativeRolls =
            characterStates.map { Rules.rollInitiative(target = it) }.sortedByDescending { it.initiativeRoll.result }

        return initiativeRolls
            .map { it.updatedTarget.character.id }
            .fold(
                State(
                    sequence = sequence,
                    updatedCharacterStates = characterStates,
                    initiativeRolls = initiativeRolls,
                    turns = listOf()
                )
            ) { roundState, turnTakerId ->
                if (roundState.hasNoWinner()) {

                    val turn =
                        Turn(takerId = turnTakerId, characterStates = roundState.updatedCharacterStates).simulate(battleground)

                    roundState.copy(
                        updatedCharacterStates = turn.updatedCharacterStates,
                        turns = roundState.turns + turn
                    )
                } else {
                    return roundState
                }
            }
    }

    data class State(
        val sequence: Int,
        val updatedCharacterStates: List<CharacterState>,
        val initiativeRolls: List<Event.InitiativeRolled>,
        val turns: List<Turn.State>
    ) {
        fun hasNoWinner(): Boolean {
            return updatedCharacterStates.any { it.allegiance == CharacterState.Allegiance.Attacker && !it.isDying() } &&
                    updatedCharacterStates.any { it.allegiance == CharacterState.Allegiance.Defender && !it.isDying() }
        }
    }
}
