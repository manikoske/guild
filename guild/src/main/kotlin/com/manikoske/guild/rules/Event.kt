package com.manikoske.guild.rules

import com.manikoske.guild.character.CharacterState

sealed interface Event {

    val target: CharacterState
    val updatedTarget: CharacterState

    sealed interface ResolutionEvent : Event

    sealed interface WeaponAttackEvent : ResolutionEvent
    sealed interface SpellAttackEvent : ResolutionEvent

    data class InitiativeRolled(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val initiativeRoll: Roll.InitiativeRoll
    ) : Event

    data class ActionStarted(
        val actionName: String,
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val movementResult: CharacterState.Result.MovementResult,
        val spendResourcesResult: CharacterState.Result.SpendResourcesResult
    ) : Event

    data class ActionEnded(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val damageOverTimeRolls: List<Roll.DamageOverTimeRoll>,
        val healOverTimeRolls: List<Roll.HealOverTimeRoll>,
        val receiveHealingResult: CharacterState.Result.ReceiveHealingResult,
        val takeDamageResult: CharacterState.Result.TakeDamageResult,
        val tickStatusesResult: CharacterState.Result.TickStatusesResult
    ) : Event

    data class Healed(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val healRoll: Roll.HealRoll,
        val receiveHealingResult: CharacterState.Result.ReceiveHealingResult,
    ) : ResolutionEvent

    data class StatusAdded(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val addStatusResult: CharacterState.Result.AddStatusResult
    ) : ResolutionEvent

    data class StatusesRemoved(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val removeStatusesResult: CharacterState.Result.RemoveStatusesResult
    ) : ResolutionEvent

    data class ResourceBoosted(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val boostResourcesResult: CharacterState.Result.BoostResourcesResult
    ) : ResolutionEvent

    data class WeaponAttackHit(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val armorClass: DifficultyClass.ArmorClass,
        val weaponAttackRoll: Roll.WeaponAttackRoll,
        val weaponDamageRoll: Roll.WeaponDamageRoll,
        val takeDamageResult: CharacterState.Result.TakeDamageResult,
        val addStatusResult: CharacterState.Result.AddStatusResult,
    ) : WeaponAttackEvent

    data class WeaponAttackMissed(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val armorClass: DifficultyClass.ArmorClass,
        val weaponAttackRoll: Roll.WeaponAttackRoll,
    ) : WeaponAttackEvent


    data class SpellAttackHit(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val spellDefenseRoll: Roll.SpellDefenseRoll,
        val spellAttackDifficultyClass: DifficultyClass.SpellAttackDifficultyClass,
        val spellDamageRoll: Roll.SpellDamageRoll,
        val takeDamageResult: CharacterState.Result.TakeDamageResult,
        val addStatusResult: CharacterState.Result.AddStatusResult,

    ) : SpellAttackEvent

    data class SpellAttackMissed(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val spellDefenseRoll: Roll.SpellDefenseRoll,
        val spellAttackDifficultyClass: DifficultyClass.SpellAttackDifficultyClass,
    ) : SpellAttackEvent

}