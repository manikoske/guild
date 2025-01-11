package com.manikoske.guild.action

sealed interface Movement {
    val nodes: Int

    data class NormalMovement(override val nodes: Int) : Movement
    data class SpecialMovement(override val nodes: Int) : Movement
}
