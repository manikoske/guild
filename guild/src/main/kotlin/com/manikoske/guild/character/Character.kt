package com.manikoske.guild.character

import com.manikoske.guild.inventory.Inventory
import com.manikoske.guild.rules.*

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

    fun maxHp(): Int {
        return (bio.clazz.hpDie.sides + attribute(Attribute.Type.constitution).modifier()) * level.level
    }

    fun maxResources(): Int {
        return bio.clazz.baseResources * level.level
    }

    private fun weaponAttackRollAttributeType(): Attribute.Type {
        return if (arms().isFinesse() && attribute(Attribute.Type.dexterity).modifier() > attribute(Attribute.Type.strength).modifier()) {
            Attribute.Type.dexterity
        } else {
            Attribute.Type.strength
        }
    }

    fun weaponAttackRoll(attackRollBonusModifier: Int): Int {
        return Die.d20.roll(1) + attribute(weaponAttackRollAttributeType()).modifier() + level.modifier() + attackRollBonusModifier
    }

    fun weaponDamageRoll(roll: () -> Int, abilityMultiplier : Int): Int {
        return roll.invoke() * abilityMultiplier + attribute(weaponAttackRollAttributeType()).modifier() + level.modifier()
    }

    fun attributeRoll(attributeType: Attribute.Type, damageRoll: () -> Int): Int {
        return damageRoll.invoke() + attribute(attributeType).modifier() + level.modifier()
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