package com.manikoske.guild.character

import com.manikoske.guild.ability.Status

data class Current(
    val damageTaken: Int,
    val resourcesSpent: Int,
    val statuses: List<Status>
) {
}