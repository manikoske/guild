package com.manikoske.guild.character

import com.manikoske.guild.ability.Effect
import com.manikoske.guild.inventory.Inventory
import com.manikoske.guild.rules.*
import kotlin.math.max

class Character(
    private val innate: Innate,
    private var current: Current,
    private val level: Level,
    private val inventory: Inventory,
) {


    // vytihnut do dakeho interface alebo podclassy
    private fun attribute(attributeType: Attribute.Type): Attribute {
        return when (attributeType) {
            Attribute.Type.strength -> innate.strength
            Attribute.Type.dexterity -> innate.dexterity
            Attribute.Type.constitution -> innate.constitution
            Attribute.Type.wisdom -> innate.wisdom
            Attribute.Type.intelligence -> innate.intelligence
            Attribute.Type.charisma -> innate.charisma
        }
    }

    fun armorClass(): Int {
        return inventory.armor.armorClass +
                inventory.armor.dexterityModifierLimit(attribute(Attribute.Type.dexterity).modifier()) +
                level.modifier() +
                inventory.arms.armorClassBonus()
    }

    fun currentHitPoints(): Int {
        return (innate.clazz.hpDie.sides + attribute(Attribute.Type.constitution).modifier()) * level.level - current.damageTaken
    }

    fun currentResources(): Int {
        return innate.clazz.baseResources * level.level - current.resourcesSpent
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

    fun difficultyClassRoll(attributeType: Attribute.Type): Int {
        return Die.d20.roll(1) + attribute(attributeType).modifier() + level.modifier()
    }

    fun difficultyClassBonus(attributeType: Attribute.Type): Int {
        return attribute(attributeType).modifier() + level.modifier()
    }

    fun isClass(): Class {
        return innate.clazz
    }

    fun arms(): Inventory.Arms {
        return inventory.arms
    }

    fun takeDamage(hitPoints: Int) {
        current = current.copy(damageTaken = max(0, current.damageTaken + hitPoints))
    }

    fun heal(hitPoints: Int) {
        current = current.copy(damageTaken = max(0, current.damageTaken - hitPoints))
    }

    fun spendResources(amount: Int) {
        current = current.copy(resourcesSpent =  max(0, current.resourcesSpent + amount))
    }

    fun gainResources(amount: Int) {
        current = current.copy(resourcesSpent =  max(0, current.resourcesSpent - amount))
    }


    fun applyEffect(effect: Effect) {
        current = current.copy(effects = current.effects + effect)
    }


//    createCharacter(base)
//    resolve()
//    levelUp()


}