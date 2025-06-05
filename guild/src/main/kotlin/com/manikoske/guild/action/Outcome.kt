package com.manikoske.guild.action

import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.rules.Die

sealed interface Outcome {

    val updatedTarget: CharacterState

    sealed interface WeaponAttackOutcome : Outcome
    sealed interface SpellAttackOutcome : Outcome

    data class Healed(
        override val updatedTarget: CharacterState,
        val healRoll: HealRoll
    ) : Outcome

    data class EffectAdded(
        override val updatedTarget: CharacterState,
        val category: Effect.Category
    ) : Outcome

    data class EffectRemoved(
        override val updatedTarget: CharacterState,
        val category: Effect.Category
    ) : Outcome

    data class ResourceBoosted(
        override val updatedTarget: CharacterState,
        val amount: Int
    ) : Outcome

    data class ActionTaken(
        override val updatedTarget: CharacterState,
        val name: String,
        val resourceCost: Int,
        val newPositionNodeId: Int
    ) : Outcome

    data class EffectsTicked(
        override val updatedTarget: CharacterState,
        val removedEffects: List<Effect>,
        val updatedEffects: List<Effect>,
        val damageOverTimeRolls: List<DamageOverTimeRoll>,
        val healOverTimeRolls: List<HealOverTimeRoll>,
    ) : Outcome

    data class WeaponAttackHit(
        override val updatedTarget: CharacterState,
        val armorClass: ArmorClass,
        val weaponAttackRoll: WeaponAttackRoll,
        val weaponDamageRoll: WeaponDamageRoll,
        val effectsRemovedByDamage: List<Effect>,
        val effectsAddedByDamage: List<Effect>

    ) : WeaponAttackOutcome

    data class WeaponAttackMissed(
        override val updatedTarget: CharacterState,
        val armorClass: ArmorClass,
        val weaponAttackRoll: WeaponAttackRoll,
    ) : WeaponAttackOutcome

    data class SpellAttackMissed(
        override val updatedTarget: CharacterState,
        val spellDefenseRoll: SpellDefenseRoll,
        val spellAttackDifficultyClass: SpellAttackDifficultyClass,
    ) : SpellAttackOutcome

    data class SpellAttackHit(
        override val updatedTarget: CharacterState,
        val spellDefenseRoll: SpellDefenseRoll,
        val spellAttackDifficultyClass: SpellAttackDifficultyClass,
        val spellDamageRoll: SpellDamageRoll,
        val effectsRemovedByDamage: List<Effect>,
        val effectsAddedByDamage: List<Effect>

    ) : SpellAttackOutcome

    data class ArmorClass(
        val armorModifier: Int,
        val armsModifier: Int,
        val levelModifier: Int,
        val armorAttributeModifier: Int
    ) {
        val armorClass = armorModifier + armsModifier + levelModifier + armorAttributeModifier
    }

    data class WeaponAttackRoll(
        val weaponAttributeModifier: Int,
        val weaponAttackModifier: Int,
        val actionAttackModifier: Int,
        val levelModifier: Int,
        val roll: Die.Roll,
    ) {
        val attack = roll.rolled + weaponAttackModifier + weaponAttackModifier + actionAttackModifier + levelModifier
    }

    data class WeaponDamageRoll(
        val weaponAttributeModifier: Int,
        val actionDamageMultiplier: Int,
        val levelModifier: Int,
        val roll: Die.Roll,
    ) {
        val damage = roll.rolled * actionDamageMultiplier + weaponAttributeModifier + levelModifier
    }


    data class DamageOverTimeRoll(
        val category: Effect.Category,
        val roll: Die.Roll
    )

    data class HealOverTimeRoll(
        val category: Effect.Category,
        val roll: Die.Roll
    )

    data class SpellAttackDifficultyClass(
        val spellAttributeModifier: Int,
        val spellDifficultyClass: Int,
        val levelModifier: Int,
    ) {
        val attack = spellAttributeModifier + spellDifficultyClass + levelModifier
    }

    data class SpellDefenseRoll(
        val spellAttributeModifier: Int,
        val levelModifier: Int,
        val roll: Die.Roll,
    ) {
        val defense = roll.rolled + spellAttributeModifier + levelModifier
    }

    data class SpellDamageRoll(
        val spellAttributeModifier: Int,
        val levelModifier: Int,
        val roll: Die.Roll,
    ) {
        val damage = roll.rolled + spellAttributeModifier + levelModifier
    }

    data class HealRoll(
        val healAttributeModifier: Int,
        val levelModifier: Int,
        val roll: Die.Roll,
    ) {
        val heal = roll.rolled + healAttributeModifier + levelModifier
    }

}