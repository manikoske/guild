package com.manikoske.guild.encounter

import com.manikoske.guild.character.Character


data class EncounterState(val characterStates : List<CharacterState>) {


    companion object {
        fun initial(
            attackers: Set<Character>,
            defenders: Set<Character>,
            attackersStartingNodeId: Int,
            defendersStartingNodeId: Int,
        ) : EncounterState {
            return EncounterState(
                characterStates =
                    attackers.map {
                        initialCharacterState(
                            character = it,
                            startingNodeId = attackersStartingNodeId,
                            allegiance = CharacterState.Allegiance.Attacker
                        )
                    }
                    +
                    defenders.map {
                        initialCharacterState(
                            character = it,
                            startingNodeId = defendersStartingNodeId,
                            allegiance = CharacterState.Allegiance.Defender
                        )
                    }
            )
        }
        private fun initialCharacterState(
            character: Character,
            startingNodeId: Int,
            allegiance: CharacterState.Allegiance
        ) : CharacterState {
            return CharacterState(
                character = character,
                positionNodeId = startingNodeId,
                allegiance = allegiance,
                damageTaken = 0,
                resourcesSpent = 0,
                effects = CharacterState.noEffects(),
            )
        }
    }

    fun hasNoWinner(): Boolean {
        return characterStates.any { it.allegiance == CharacterState.Allegiance.Attacker && !it.isDying() } &&
                characterStates.any { it.allegiance == CharacterState.Allegiance.Defender && !it.isDying() }
    }



    fun rollInitiatives() : List<Int> {
        return characterStates.sortedByDescending { it.character.initiativeRoll() }.map { it.character.id }
    }

}