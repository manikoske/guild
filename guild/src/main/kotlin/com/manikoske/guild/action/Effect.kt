package com.manikoske.guild.action

import com.manikoske.guild.character.Attribute

sealed interface Effect {

    val savingThrow: SavingThrow

    data class WeaponDamage(
        val damageRoll: () -> Int,
        val attackRollBonusModifier: Int,
        val damageRollMultiplier: Int
    ) : Effect {
        override val savingThrow: SavingThrow
            get() = SavingThrow.ArmorClassSavingThrow(attackRollBonusModifier)

    }

    data class AvoidableDamage(
        val baseDifficultyClass: Int,
        val executorAttributeType: Attribute.Type,
        val targetAttributeType: Attribute.Type,
        val damageRoll: () -> Int
    ) : Effect {
        override val savingThrow: SavingThrow
            get() = SavingThrow.DifficultyClassSavingThrow(
                baseDifficultyClass,
                executorAttributeType,
                targetAttributeType
            )
    }

    data class DirectDamage(
        val damageRoll: () -> Int
    ) : Effect {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave

    }

    data class Healing(
        val healingRoll: () -> Int
    ) : Effect {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave
    }

    data class ResourceBoost(
        val amount: Int
    ) : Effect {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave

    }

    data class AddStatus(
        val baseDifficultyClass: Int,
        val executorAttributeType: Attribute.Type,
        val targetAttributeType: Attribute.Type,
        val status: Status
    ) : Effect {
        override val savingThrow: SavingThrow
            get() = SavingThrow.DifficultyClassSavingThrow(
                baseDifficultyClass,
                executorAttributeType,
                targetAttributeType
            )
    }

    data class RemoveStatus(
        val status: Status
    ) : Effect {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave
    }

    data class AddBuffStatus(
        val status: Status
    ) : Effect {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave
    }

    data object NoEffect : Effect {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave
    }

}