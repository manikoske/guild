package com.manikoske.guild.action

sealed interface TriggeredAction {

    data class SelfTriggeredAction(
        val effect: Effect
    ) : TriggeredAction

    data class TargetTriggeredAction(
        val effect: Effect
    ) : TriggeredAction
}