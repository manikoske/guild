package com.manikoske.guild.encounter

import com.manikoske.guild.character.Character

class Encounter(
    private val battleground: Battleground
) {

    fun simulate(
        attackers: Set<Character>,
        defenders: Set<Character>,
        attackersStartingNodeId: Int,
        defendersStartingNodeId: Int
    ): State {
        // Create initial character states
        val initialState = State(
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
                } +
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

        // Run the simulation without logging
        val result = (1..20).fold(initialState) { encounterState, roundSequence ->
            val round = Round(sequence = roundSequence, characterStates = encounterState.updatedCharacterStates)
                .simulate(battleground)

            if (round.turns.isEmpty()) {
                encounterState
            } else {
                State(
                    updatedCharacterStates = round.updatedCharacterStates,
                    rounds = encounterState.rounds + round
                )
            }
        }

        return result
    }

    data class State(
        val updatedCharacterStates : List<CharacterState>,
        val rounds : List<Round.State>
    )

}
