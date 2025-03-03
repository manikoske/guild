package com.manikoske.guild.inventory

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.rules.Die
import com.manikoske.guild.rules.Rollable

sealed interface Weapon {

    object Weapons {

        val longSword = MeleeWeapon(
            name = "Long Sword",
            damage = Rollable.Damage(roll = { Die.d8.roll(1) }),
            properties = listOf(),
            attributeType = Attribute.Type.strength
        )

        val axe = MeleeWeapon(
            name = "Axe",
            damage = Rollable.Damage(roll = { Die.d8.roll(1) }),
            properties = listOf(),
            attributeType = Attribute.Type.strength
        )

        val shortSword = MeleeWeapon(
            name = "Short Sword",
            damage = Rollable.Damage(roll = { Die.d6.roll(1) }),
            properties = listOf(Property.light),
            attributeType = Attribute.Type.dexterity
        )

        val dagger = MeleeWeapon(
            name = "Dagger",
            damage = Rollable.Damage(roll = { Die.d4.roll(1) }),
            properties = listOf(Property.light),
            attributeType = Attribute.Type.dexterity
        )

        val greatSword = MeleeWeapon(
            name = "Great Sword",
            damage = Rollable.Damage(roll = { Die.d6.roll(2) }),
            properties = listOf(Property.twoHanded),
            attributeType = Attribute.Type.strength
        )

        val greatAxe = MeleeWeapon(
            name = "Great Axe",
            damage = Rollable.Damage(roll = { Die.d12.roll(1) }),
            properties = listOf(Property.twoHanded),
            attributeType = Attribute.Type.strength
        )

        val spear = MeleeWeapon(
            name = "Spear",
            damage = Rollable.Damage(roll = { Die.d10.roll(1) }),
            properties = listOf(Property.twoHanded),
            attributeType = Attribute.Type.dexterity
        )

        val warHammer = MeleeWeapon(
            name = "War Hammer",
            damage = Rollable.Damage(roll = { Die.d8.roll(1) }),
            properties = listOf(),
            attributeType = Attribute.Type.strength
        )

        val mace = MeleeWeapon(
            name = "Mace",
            damage = Rollable.Damage(roll = { Die.d6.roll(1) }),
            properties = listOf(),
            attributeType = Attribute.Type.strength
        )

        val quarterStaff = MeleeWeapon(
            name = "Quarterstaff",
            damage = Rollable.Damage(roll = { Die.d6.roll(1) }),
            properties = listOf(Property.twoHanded),
            attributeType = Attribute.Type.dexterity
        )

        val sling = RangedWeapon(
            name = "Sling",
            damage = Rollable.Damage(roll = { Die.d4.roll(1) }),
            properties = listOf(),
            range = 1,
            attributeType = Attribute.Type.dexterity
        )

        val shortBow = RangedWeapon(
            name = "Short Bow",
            damage = Rollable.Damage(roll = { Die.d6.roll(1) }),
            properties = listOf(),
            range = 2,
            attributeType = Attribute.Type.dexterity
        )

        val longBow = RangedWeapon(
            name = "Long Bow",
            damage = Rollable.Damage(roll = { Die.d8.roll(1) }),
            properties = listOf(),
            range = 3,
            attributeType = Attribute.Type.dexterity
        )

    }

    val name: String
    val damage: Rollable.Damage
    val properties: List<Property>

    fun attributeType(): Attribute.Type

    data class MeleeWeapon(
        override val name: String,
        override val damage: Rollable.Damage,
        override val properties: List<Property>,
        val attributeType: Attribute.Type
    ): Weapon {

        override fun attributeType(): Attribute.Type {
            return attributeType
        }
    }

    data class RangedWeapon(
        override val name: String,
        override val damage: Rollable.Damage,
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