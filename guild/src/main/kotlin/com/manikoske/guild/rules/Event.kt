package com.manikoske.guild.rules

import com.manikoske.guild.character.Effect
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
        val newPositionNodeId: Int,
        val resourcesSpent: Int,
        val effectsRemovedByMovement: List<Effect>,
    ) : Event

    data class ActionEnded(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val removedEffects: List<Effect>,
        val updatedEffects: List<Effect>,
        val damageOverTimeRolls: List<Roll.DamageOverTimeRoll>,
        val healOverTimeRolls: List<Roll.HealOverTimeRoll>,
    ) : Event

    data class Healed(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val healRoll: Roll.HealRoll
    ) : ResolutionEvent

    data class EffectAdded(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val effects: List<Effect>
    ) : ResolutionEvent

    data class EffectRemoved(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val effects: List<Effect>
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
        val effectsRemovedByDamage: List<Effect>,
        val effectsAddedByDamage: List<Effect>

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
        val effectsRemovedByDamage: List<Effect>,
        val effectsAddedByDamage: List<Effect>

    ) : SpellAttackEvent

}