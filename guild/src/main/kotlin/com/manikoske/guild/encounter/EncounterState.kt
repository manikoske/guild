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

        fun fromView(pointOfView: PointOfView) : EncounterState {
            return EncounterState(characterStates = pointOfView.allies + pointOfView.enemies + pointOfView.taker)
        }
    }

    fun hasNoWinner(): Boolean {
        return characterStates.any { it.allegiance == CharacterState.Allegiance.Attacker && !it.isDying() } &&
                characterStates.any { it.allegiance == CharacterState.Allegiance.Defender && !it.isDying() }
    }

    fun viewFrom(
        characterId: Int,
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

    fun rollInitiatives() : List<Int> {
        return characterStates.sortedByDescending { it.character.initiativeRoll() }.map { it.character.id }
    }

}