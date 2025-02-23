package com.manikoske.guild.character

import com.manikoske.guild.action.Outcome
import com.manikoske.guild.inventory.Inventory
import com.manikoske.guild.rules.*
import kotlin.math.max

data class Character(
    val id: Int,
    private val bio: Bio,
    private val level: Level,
    private val inventory: Inventory,
) {

    private fun attribute(attributeType: Attribute.Type): Attribute {
        return when (attributeType) {
            Attribute.Type.strength -> bio.strength
            Attribute.Type.dexterity -> bio.dexterity
            Attribute.Type.constitution -> bio.constitution
            Attribute.Type.wisdom -> bio.wisdom
            Attribute.Type.intelligence -> bio.intelligence
            Attribute.Type.charisma -> bio.charisma
        }
    }

    fun armorClass(): Int {
        return inventory.armor.armorClass +
                inventory.armor.dexterityModifierLimit(attribute(Attribute.Type.dexterity).modifier()) +
                level.modifier() +
                inventory.arms.armorClassBonus()
    }

    fun maxHitPoints(): Int {
        return (bio.clazz.hpDie.sides + attribute(Attribute.Type.constitution).modifier()) * level.level
    }

    fun maxResources(): Int {
        return bio.clazz.baseResources * level.level
    }

    private fun weaponAttributeModifier(): Int {
        if (arms().isFinesse()) {
            return max(attribute(Attribute.Type.dexterity).modifier(), attribute(Attribute.Type.strength).modifier())
        } else {
            return attribute(Attribute.Type.strength).modifier()
        }
    }

    fun weaponAttackRoll(attackRollBonusModifier: Int): Int {
        val weaponAttackBonus = when (val arms = arms()) {
            is Inventory.Arms.DualWeapon -> -2
            is Inventory.Arms.OneHandedWeaponAndShield -> 0
            is Inventory.Arms.TwoHandedWeapon -> 0
            is Inventory.Arms.RangedWeapon -> 0
        }
        return Die.d20.roll(1) + weaponAttributeModifier() + level.modifier() + attackRollBonusModifier + weaponAttackBonus
    }

    fun weaponDamageRoll(damageRollMultiplier : Int): Int {
        val rolledWeaponDamage = when (val arms = arms()) {
            is Inventory.Arms.DualWeapon -> arms.mainHand.damageRoll.invoke() + arms.offHand.damageRoll.invoke()
            is Inventory.Arms.OneHandedWeaponAndShield -> arms.mainHand.damageRoll.invoke()
            is Inventory.Arms.TwoHandedWeapon -> arms.bothHands.damageRoll.invoke()
            is Inventory.Arms.RangedWeapon -> arms.bothHands.damageRoll.invoke()
        }
        return rolledWeaponDamage * damageRollMultiplier + weaponAttributeModifier() + level.modifier()
    }

    fun attributeRoll(attributeType: Attribute.Type, roll: () -> Int): Int {
        return roll.invoke() + attribute(attributeType).modifier() + level.modifier()
    }

    fun initiativeRoll() : Int {
        return Die.d20.roll(1) + attribute(Attribute.Type.dexterity).modifier()
    }

    fun difficultyClassRoll(attributeType: Attribute.Type): Int {
        return Die.d20.roll(1) + attribute(attributeType).modifier() + level.modifier()
    }

    fun difficultyClassBonus(attributeType: Attribute.Type): Int {
        return attribute(attributeType).modifier() + level.modifier()
    }

    fun clazz(): Class {
        return bio.clazz
    }

    fun arms(): Inventory.Arms {
        return inventory.arms
    }
}