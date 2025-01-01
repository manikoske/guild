package com.manikoske.guild.character

import com.manikoske.guild.ability.Effect

data class Current(
    val damageTaken: Int,
    val resourcesSpent: Int,
    val effects: List<Effect>
) {
}