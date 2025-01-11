package com.manikoske.guild.action

import com.manikoske.guild.character.Character
import com.manikoske.guild.character.Attribute

sealed interface SavingThrow {

    fun saved(executor: Character, target: Character): Boolean

    data class ArmorClassSavingThrow(val attackRollBonusModifier: Int) : SavingThrow {
        override fun saved(executor: Character, target: Character): Boolean {
            return executor.weaponAttackRoll(attackRollBonusModifier) < target.armorClass()
        }
    }

    data object NoSave : SavingThrow {
        override fun saved(executor: Character, target: Character): Boolean {
            return false
        }
    }

    data class DifficultyClassSavingThrow(
        val baseDifficulty: Int,
        val executorAttributeType: Attribute.Type,
        val targetAttributeType: Attribute.Type
    ) : SavingThrow {
        override fun saved(executor: Character, target: Character): Boolean {
            return target.difficultyClassRoll(targetAttributeType) >= baseDifficulty + executor.difficultyClassBonus(
                executorAttributeType
            )
        }
    }

}