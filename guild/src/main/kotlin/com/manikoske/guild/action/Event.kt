package com.manikoske.guild.action

sealed interface Event {

    data class WeaponAttackHit(val damage: Int) : Event
    data class WeaponAttackMiss(val damage: Int) : Event


}