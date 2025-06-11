package com.manikoske.guild.encounter

data class RoundState(
    val updatedCharacterStates : List<CharacterState>,
    val turns : List<TurnState>
)
