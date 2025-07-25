package com.manikoske.guild.inventory

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.rules.Dice
import com.manikoske.guild.rules.Die

sealed interface Weapon {

    object Weapons {

        val longSword = MeleeWeapon(
            name = "Long Sword",
            damageDice = Dice.of(Die.d8),
            properties = listOf(),
            attributeType = Attribute.Type.strength
        )

        val axe = MeleeWeapon(
            name = "Axe",
            damageDice = Dice.of(Die.d8),
            properties = listOf(),
            attributeType = Attribute.Type.strength
        )

        val shortSword = MeleeWeapon(
            name = "Short Sword",
            damageDice = Dice.of(Die.d6),
            properties = listOf(Property.light),
            attributeType = Attribute.Type.dexterity
        )

        val dagger = MeleeWeapon(
            name = "Dagger",
            damageDice = Dice.of(Die.d4),
            properties = listOf(Property.light),
            attributeType = Attribute.Type.dexterity
        )

        val greatSword = MeleeWeapon(
            name = "Great Sword",
            damageDice = Dice.of(2, Die.d6),
            properties = listOf(Property.twoHanded),
            attributeType = Attribute.Type.strength
        )

        val greatAxe = MeleeWeapon(
            name = "Great Axe",
            damageDice = Dice.of(Die.d12),
            properties = listOf(Property.twoHanded),
            attributeType = Attribute.Type.strength
        )

        val spear = MeleeWeapon(
            name = "Spear",
            damageDice = Dice.of(Die.d10),
            properties = listOf(Property.twoHanded),
            attributeType = Attribute.Type.dexterity
        )

        val warHammer = MeleeWeapon(
            name = "War Hammer",
            damageDice = Dice.of(Die.d8),
            properties = listOf(),
            attributeType = Attribute.Type.strength
        )

        val mace = MeleeWeapon(
            name = "Mace",
            damageDice = Dice.of(Die.d6),
            properties = listOf(),
            attributeType = Attribute.Type.strength
        )

        val quarterStaff = MeleeWeapon(
            name = "Quarterstaff",
            damageDice = Dice.of(Die.d6),
            properties = listOf(Property.twoHanded),
            attributeType = Attribute.Type.dexterity
        )

        val sling = RangedWeapon(
            name = "Sling",
            damageDice = Dice.of(Die.d4),
            properties = listOf(),
            range = 1,
            attributeType = Attribute.Type.dexterity
        )

        val shortBow = RangedWeapon(
            name = "Short Bow",
            damageDice = Dice.of(Die.d6),
            properties = listOf(),
            range = 2,
            attributeType = Attribute.Type.dexterity
        )

        val longBow = RangedWeapon(
            name = "Long Bow",
            damageDice = Dice.of(Die.d8),
            properties = listOf(),
            range = 3,
            attributeType = Attribute.Type.dexterity
        )

    }

    val name: String
    val damageDice: Dice
    val properties: List<Property>

    fun attributeType(): Attribute.Type

    data class MeleeWeapon(
        override val name: String,
        override val damageDice: Dice,
        override val properties: List<Property>,
        val attributeType: Attribute.Type
    ): Weapon {

        override fun attributeType(): Attribute.Type {
            return attributeType
        }
    }

    data class RangedWeapon(
        override val name: String,
        override val damageDice: Dice,
        override val properties: List<Property>,
        val attributeType: Attribute.Type,
        val range: Int
    ): Weapon {

        override fun attributeType(): Attribute.Type {
            return attributeType
        }
    }

    enum class Property {
        twoHanded, light
    }

}