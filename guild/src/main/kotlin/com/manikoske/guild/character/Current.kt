package com.manikoske.guild.character

import com.manikoske.guild.action.Status

data class Current(
    val damageTaken: Int,
    val resourcesSpent: Int,
    val statuses: List<Status>
) {
}