package com.manikoske.guild.ability

interface TriggeredAction {

    data class SelfTriggeredAction(
        val effect: Effect
    ) : TriggeredAction

    data class TargetTriggeredAction(
        val effect: Effect
    ) : TriggeredAction
}