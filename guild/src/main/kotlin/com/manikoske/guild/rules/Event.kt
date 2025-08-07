package com.manikoske.guild.rules

import com.manikoske.guild.character.CharacterState
import com.manikoske.guild.character.Status

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
        val newPositionNodeId: Int,
        val resourcesSpent: Int,
        val statusesRemovedOnMovement: List<Status>,
    ) : Event

    data class ActionEnded(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val removedStatuses: List<Status>,
        val updatedStatuses: List<Status>,
        val damageOverTimeRolls: List<Roll.DamageOverTimeRoll>,
        val healOverTimeRolls: List<Roll.HealOverTimeRoll>,
    ) : Event

    data class Healed(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val healRoll: Roll.HealRoll
    ) : ResolutionEvent

    data class StatusAdded(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val status: Status
    ) : ResolutionEvent

    data class StatusesRemoved(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val statuses: List<Status>
    ) : ResolutionEvent

    data class ResourceBoosted(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val amount: Int
    ) : ResolutionEvent

    data class WeaponAttackHit(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val armorClass: DifficultyClass.ArmorClass,
        val weaponAttackRoll: Roll.WeaponAttackRoll,
        val weaponDamageRoll: Roll.WeaponDamageRoll,
        val statusesRemovedByDamage: List<Status>,
        val statusAddedByDamage: Status

    ) : WeaponAttackEvent

    data class WeaponAttackMissed(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val armorClass: DifficultyClass.ArmorClass,
        val weaponAttackRoll: Roll.WeaponAttackRoll,
    ) : WeaponAttackEvent

    data class SpellAttackMissed(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val spellDefenseRoll: Roll.SpellDefenseRoll,
        val spellAttackDifficultyClass: DifficultyClass.SpellAttackDifficultyClass,
    ) : SpellAttackEvent

    data class SpellAttackHit(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val spellDefenseRoll: Roll.SpellDefenseRoll,
        val spellAttackDifficultyClass: DifficultyClass.SpellAttackDifficultyClass,
        val spellDamageRoll: Roll.SpellDamageRoll,
        val statusesRemovedByDamage: List<Status>,
        val statusAddedByDamage: Status

    ) : SpellAttackEvent

}