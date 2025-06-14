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
    ): State {

        return (1..20).fold(
            State(
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
                return State(
                    updatedCharacterStates = round.updatedCharacterStates,
                    rounds = encounterState.rounds + round
                )
            }
        }
    }

    data class State(
        val updatedCharacterStates : List<CharacterState>,
        val rounds : List<Round.State>
    )

}
