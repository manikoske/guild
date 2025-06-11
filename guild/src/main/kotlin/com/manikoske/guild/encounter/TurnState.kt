package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Event

data class TurnState(
    val updatedPointOfView: PointOfView,
    val action: Action,
    val target: Target,
    val actionTaken: Event.ActionTaken,
    val effectsTicked: Event.EffectsTicked,
    val selfEvent : Event,
    val targetEvents: List<Event>
) {
    fun utility(): Double {
        return updatedPointOfView.allies.sumOf { it.utility() } - updatedPointOfView.enemies.sumOf { it.utility() }
    }

    fun updatedCharacterStates() : List<CharacterState> {
        return updatedPointOfView.allies + updatedPointOfView.enemies + updatedPointOfView.taker
    }
}
