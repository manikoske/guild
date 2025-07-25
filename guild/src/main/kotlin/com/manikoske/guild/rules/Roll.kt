package com.manikoske.guild.rules

import com.manikoske.guild.character.Effect

sealed interface Roll {

    data class Rolled(
        val dice: Dice,
        val rollMethod: Dice.RollMethod
    ) {
        val result = dice.roll(rollMethod)
    }

    val rolled: Rolled
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
        override val rolled: Rolled,
    ) : Roll, HasAttributeModifier, HasLevelModifier {
        override val result = rolled.result + weaponAttackModifier + weaponAttackModifier + actionAttackModifier + levelModifier
    }

    data class WeaponDamageRoll(
        override val attributeModifier: Int,
        val actionDamageMultiplier: Int,
        override val levelModifier: Int,
        override val rolled: Rolled,
    ) : Roll, HasAttributeModifier, HasLevelModifier {
        override val result = rolled.result * actionDamageMultiplier + attributeModifier + levelModifier
    }

    data class DamageOverTimeRoll(
        val effect: Effect,
        override val rolled: Rolled
    ) : Roll {
        override val result = rolled.result
    }

    data class HealOverTimeRoll(
        val effect: Effect,
        override val rolled: Rolled
    ) : Roll {
        override val result = rolled.result
    }

    data class SpellDefenseRoll(
        override val attributeModifier: Int,
        override val levelModifier: Int,
        override val rolled: Rolled,
    ) : Roll, HasAttributeModifier, HasLevelModifier {
        override val result = rolled.result + attributeModifier + levelModifier
    }

    data class SpellDamageRoll(
        override val attributeModifier: Int,
        override val levelModifier: Int,
        override val rolled: Rolled,
    ) : Roll, HasAttributeModifier, HasLevelModifier {
        override val result = rolled.result + attributeModifier + levelModifier
    }

    data class HealRoll(
        override val attributeModifier: Int,
        override val levelModifier: Int,
        override val rolled: Rolled,
    ) : Roll, HasAttributeModifier, HasLevelModifier {
        override val result = rolled.result + attributeModifier + levelModifier
    }

    data class InitiativeRoll(
        override val attributeModifier: Int,
        override val levelModifier: Int,
        override val rolled: Rolled,
    ) : Roll, HasAttributeModifier, HasLevelModifier {
        override val result = rolled.result + attributeModifier + levelModifier
    }
}