package com.manikoske.guild.encounter

import com.manikoske.guild.character.Character


data class EncounterState(
    val updatedCharacterStates : List<CharacterState>,
    val rounds : List<RoundState>
) {


}