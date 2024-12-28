package com.manikoske.guild.character

import com.manikoske.guild.rules.Attribute
import com.manikoske.guild.rules.Die
import com.manikoske.guild.rules.Inventory
import com.manikoske.guild.rules.Level

class Character(
    private val innate: Innate,
    private val current: Current,
    private val level: Level,
    private val inventory: Inventory,
) {


    // vytihnut do dakeho interface alebo podclassy
    fun attribute(attributeType: Attribute.Type): Attribute {
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
                (inventory.meleeOffHand?.armorClassBonus ?: 0)
    }

    fun currentHitPoints(): Int {
        return (innate.clazz.hpDie.sides + attribute(Attribute.Type.constitution).modifier()) * level.level - current.damageTaken
    }

    fun currentResources(): Int {
        return innate.clazz.baseResources * level.level - current.resourcesSpent
    }

    fun attackRoll(attributeType: Attribute.Type, modifier: Int): Int {
        return Die.d20.roll(1) + attribute(attributeType).modifier() + level.modifier() + modifier
    }

    fun damageRoll(attributeType: Attribute.Type, rolls: List<() -> Int>): Int {
        return rolls.sumOf { roll -> roll.invoke() } + attribute(attributeType).modifier() + level.modifier()
    }


//    createCharacter(base)
//    resolve()
//    levelUp()


}