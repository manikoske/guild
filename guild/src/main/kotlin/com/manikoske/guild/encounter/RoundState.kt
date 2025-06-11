package com.manikoske.guild.encounter

import com.manikoske.guild.action.Event

data class RoundState(
    val sequence: Int,
    val updatedCharacterStates : List<CharacterState>,
    val initiativeRolls: List<Event.InitiativeRolled>,
    val turns : List<TurnState>
) {
    fun viewFrom(
        characterId: Int,
    ): PointOfView {
        val taker = updatedCharacterStates.first { it.character.id == characterId }
        val allies = updatedCharacterStates.filter { it.character.id != characterId && it.allegiance == taker.allegiance }
        val enemies = updatedCharacterStates.filter { it.allegiance != taker.allegiance }

        return PointOfView(
            taker = taker,
            allies = allies,
            enemies = enemies
        )
    }

    fun hasNoWinner(): Boolean {
        return updatedCharacterStates.any { it.allegiance == CharacterState.Allegiance.Attacker && !it.isDying() } &&
                updatedCharacterStates.any { it.allegiance == CharacterState.Allegiance.Defender && !it.isDying() }
    }
}
