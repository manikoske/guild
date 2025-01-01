package com.manikoske.guild.ability

import com.manikoske.guild.character.Character
import com.manikoske.guild.character.Attribute

sealed interface Outcome {

    val savingThrow: SavingThrow

    fun apply(executor: Character, target: Character)

    fun resolve(executor: Character, target: Character) : Boolean {
        if (savingThrow.saved(executor, target)) {
            return false
        } else {
            apply(executor, target)
            return true
        }
    }

    data class WeaponDamage(
        val damageRoll: () -> Int,
        val attackRollBonusModifier: Int,
        val damageRollMultiplier: Int
    ) : Outcome {
        override val savingThrow: SavingThrow
            get() = SavingThrow.ArmorClassSavingThrow(attackRollBonusModifier)

        override fun apply(executor: Character, target: Character) {
            return target.takeDamage(executor.weaponDamageRoll(damageRoll, damageRollMultiplier))
        }
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

        override fun apply(executor: Character, target: Character) {
            return target.takeDamage(executor.attributeRoll(executorAttributeType, damageRoll))
        }
    }

    data class DirectDamage(
        val damageRoll: () -> Int
    ) : Outcome {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave

        override fun apply(executor: Character, target: Character) {
            return target.takeDamage(damageRoll.invoke())
        }
    }

    data class Healing(
        val healingRoll: () -> Int
    ) : Outcome {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave

        override fun apply(executor: Character, target: Character) {
            return target.heal(executor.attributeRoll(Attribute.Type.wisdom, healingRoll))
        }
    }

    data class ResourceBoost(
        val amount: Int
    ) : Outcome {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave

        override fun apply(executor: Character, target: Character) {
            return target.gainResources(amount)
        }
    }

    data class ApplyEffect(
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

        override fun apply(executor: Character, target: Character) {
            return target.applyEffect(effect)
        }
    }

    data class ApplyBuffEffect(
        val effect: Effect
    ) : Outcome {
        override val savingThrow: SavingThrow
            get() = SavingThrow.NoSave

        override fun apply(executor: Character, target: Character) {
            return target.applyEffect(effect)
        }
    }

}