package com.manikoske.guild.action

import com.manikoske.guild.character.CharacterState

data class Target(
    val range: Int,
    val targetedCharacterStates: List<CharacterState>,
    val type: Type
)  {

    enum class Type {
        SingleEnemy,
        SingleAlly,
        NodeAlly,
        NodeEnemy,
        NodeEveryone,
    }


}