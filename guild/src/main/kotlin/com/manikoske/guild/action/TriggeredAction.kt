package com.manikoske.guild.action

sealed interface TriggeredAction {

    data class SelfTriggeredAction(
        val outcome: Outcome
    ) : TriggeredAction

    data class TargetTriggeredAction(
        val outcome: Outcome
    ) : TriggeredAction
}