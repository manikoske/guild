package com.manikoske.guild.ability

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.Character
import com.manikoske.guild.encounter.Encounter

sealed interface Effect {

    val savingThrow: SavingThrow

    fun isHarmful() : Boolean


    data class WeaponDamage(
        val damageRoll: () -> Int,
        val attackRollBonusModifier: Int,
        val damageRollMultiplier: Int
    ) : Effect {
        override val savingThrow: SavingThrow
            get() = SavingThrow.ArmorClassSavingThrow(attackRollBonusModifier)

        override fun isHarmful() : Boolean {
            return true
        }
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

        override fun isHarmful() : Boolean {
            return true
        }
    }

    data class DirectDamage(
        val damageRoll: () -> Int
    ) : Effect {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave

        override fun isHarmful() : Boolean {
            return true
        }
    }

    data class Healing(
        val healingRoll: () -> Int
    ) : Effect {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave

        override fun isHarmful() : Boolean {
            return false
        }
    }

    data class ResourceBoost(
        val amount: Int
    ) : Effect {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave

        override fun isHarmful() : Boolean {
            return false
        }
    }

    data class ApplyStatus(
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

        override fun isHarmful() : Boolean {
            return true
        }
    }

    data class ApplyBuffStatus(
        val status: Status
    ) : Effect {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave

        override fun isHarmful() : Boolean {
            return false
        }
    }

    data object NoEffect : Effect {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave

        override fun isHarmful() : Boolean {
            return false
        }
    }

}