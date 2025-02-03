package com.manikoske.guild.action

import com.manikoske.guild.character.Attribute

sealed interface Outcome {

    val savingThrow: SavingThrow

    data class WeaponDamage(
        val damageRoll: () -> Int,
        val attackRollBonusModifier: Int,
        val damageRollMultiplier: Int
    ) : Outcome {
        override val savingThrow: SavingThrow
            get() = SavingThrow.ArmorClassSavingThrow(attackRollBonusModifier)

    }

    data class AvoidableDamage(
        val baseDifficultyClass: Int,
        val executorAttributeType: Attribute.Type,
        val targetAttributeType: Attribute.Type,
        val damageRoll: () -> Int
    ) : Outcome {
        override val savingThrow: SavingThrow
            get() = SavingThrow.DifficultyClassSavingThrow(
                baseDifficultyClass,
                executorAttributeType,
                targetAttributeType
            )
    }

    data class DirectDamage(
        val damageRoll: () -> Int
    ) : Outcome {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave

    }

    data class Healing(
        val healingRoll: () -> Int
    ) : Outcome {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave
    }

    data class ResourceBoost(
        val amount: Int
    ) : Outcome {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave

    }

    data class AddEffect(
        val baseDifficultyClass: Int,
        val executorAttributeType: Attribute.Type,
        val targetAttributeType: Attribute.Type,
        val effect: Effect
    ) : Outcome {
        override val savingThrow: SavingThrow
            get() = SavingThrow.DifficultyClassSavingThrow(
                baseDifficultyClass,
                executorAttributeType,
                targetAttributeType
            )
    }

    data class RemoveEffect(
        val effect: Effect
    ) : Outcome {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave
    }

    data class AddBuffEffect(
        val effect: Effect
    ) : Outcome {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave
    }

    data object NoOutcome : Outcome {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave
    }

}