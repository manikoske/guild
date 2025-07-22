package com.manikoske.guild.action

import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.rules.Die

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
        val effect: Effect
    ) : ResolutionEvent

    data class EffectRemoved(
        override val target: CharacterState,
        override val updatedTarget: CharacterState,
        val effect: Effect
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

    sealed interface Roll {

        val roll: Die.Roll
        val result: Int

        interface HasAttributeModifier {
            val attributeModifier: Int
        }

        interface HasLevelModifier {
            val levelModifier: Int
        }

        data class WeaponAttackRoll(
            override val attributeModifier: Int,
            val weaponAttackModifier: Int,
            val actionAttackModifier: Int,
            override val levelModifier: Int,
            override val roll: Die.Roll,
        ) : Roll, HasAttributeModifier, HasLevelModifier {
            override val result = roll.rolled + weaponAttackModifier + weaponAttackModifier + actionAttackModifier + levelModifier
        }

        data class WeaponDamageRoll(
            override val attributeModifier: Int,
            val actionDamageMultiplier: Int,
            override val levelModifier: Int,
            override val roll: Die.Roll,
        ) : Roll, HasAttributeModifier, HasLevelModifier {
            override val result = roll.rolled * actionDamageMultiplier + attributeModifier + levelModifier
        }

        data class DamageOverTimeRoll(
            val category: Effect.Category,
            override val roll: Die.Roll
        ) : Roll {
            override val result = roll.rolled
        }

        data class HealOverTimeRoll(
            val category: Effect.Category,
            override val roll: Die.Roll
        ) : Roll {
            override val result = roll.rolled
        }

        data class SpellDefenseRoll(
            override val attributeModifier: Int,
            override val levelModifier: Int,
            override val roll: Die.Roll,
        ) : Roll, HasAttributeModifier, HasLevelModifier {
            override val result = roll.rolled + attributeModifier + levelModifier
        }

        data class SpellDamageRoll(
            override val attributeModifier: Int,
            override val levelModifier: Int,
            override val roll: Die.Roll,
        ) : Roll, HasAttributeModifier, HasLevelModifier {
            override val result = roll.rolled + attributeModifier + levelModifier
        }

        data class HealRoll(
            override val attributeModifier: Int,
            override val levelModifier: Int,
            override val roll: Die.Roll,
        ) : Roll, HasAttributeModifier, HasLevelModifier {
            override val result = roll.rolled + attributeModifier + levelModifier
        }

        data class InitiativeRoll(
            override val attributeModifier: Int,
            override val levelModifier: Int,
            override val roll: Die.Roll,
        ) : Roll, HasAttributeModifier, HasLevelModifier {
            override val result = roll.rolled + attributeModifier + levelModifier
        }
    }

    sealed interface DifficultyClass {

        val result: Int
        val levelModifier: Int
        val attributeModifier: Int
        val baseDifficultyClass: Int

        data class ArmorClass(
            val armorDifficultyClass: Int,
            val armsModifier: Int,
            override val levelModifier: Int,
            val armorAttributeModifier: Int
        ) : DifficultyClass {
            override val result = armorDifficultyClass + armsModifier + levelModifier + armorAttributeModifier
            override val attributeModifier: Int
                get() = armorAttributeModifier
            override val baseDifficultyClass: Int
                get() = armorDifficultyClass
        }

        data class SpellAttackDifficultyClass(
            val spellAttributeModifier: Int,
            val spellDifficultyClass: Int,
            override val levelModifier: Int,
        ) : DifficultyClass {
            override val result = spellAttributeModifier + spellDifficultyClass + levelModifier

            override val attributeModifier: Int
                get() = spellAttributeModifier
            override val baseDifficultyClass: Int
                get() = spellDifficultyClass
        }
    }

}