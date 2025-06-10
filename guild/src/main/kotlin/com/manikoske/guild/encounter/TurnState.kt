package com.manikoske.guild.encounter

import com.manikoske.guild.action.Event

data class TurnState(
    val updatedPointOfView: PointOfView,
    val turnStart: Event.ActionTaken,
    val turnEnding: Event.EffectsTicked,
    val selfEvent : Event,
    val targetEvents: List<Event>
)
