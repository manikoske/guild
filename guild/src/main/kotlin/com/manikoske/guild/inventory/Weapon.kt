package com.manikoske.guild.inventory

import com.manikoske.guild.rules.Die

sealed interface Weapon {

    object Weapons {

        val longSword = MeleeWeapon(
            name = "Long Sword",
            damageRoll = { Die.d8.roll(1) },
            properties = listOf()
        )

        val axe = MeleeWeapon(
            name = "Axe",
            damageRoll = { Die.d8.roll(1) },
            properties = listOf()
        )

        val shortSword = MeleeWeapon(
            name = "Short Sword",
            damageRoll = { Die.d6.roll(1) },
            properties = listOf(Property.finesse, Property.light)
        )

        val dagger = MeleeWeapon(
            name = "Dagger",
            damageRoll = { Die.d4.roll(1) },
            properties = listOf(Property.finesse, Property.light)
        )

        val greatSword = MeleeWeapon(
            name = "Great Sword",
            damageRoll = { Die.d6.roll(2) },
            properties = listOf(Property.twoHanded)
        )

        val greatAxe = MeleeWeapon(
            name = "Great Axe",
            damageRoll = { Die.d12.roll(1) },
            properties = listOf(Property.twoHanded)
        )

        val warHammer = MeleeWeapon(
            name = "War Hammer",
            damageRoll = { Die.d8.roll(1) },
            properties = listOf()
        )

        val mace = MeleeWeapon(
            name = "Mace",
            damageRoll = { Die.d6.roll(1) },
            properties = listOf()
        )

        val quarterStaff = MeleeWeapon(
            name = "Quarterstaff",
            damageRoll = { Die.d6.roll(1) },
            properties = listOf(Property.twoHanded)
        )

        val sling = RangedWeapon(
            name = "Sling",
            damageRoll = { Die.d4.roll(1) },
            properties = listOf(),
            range = 1,
        )

        val shortBow = RangedWeapon(
            name = "Short Bow",
            damageRoll = { Die.d6.roll(1) },
            properties = listOf(),
            range = 2,
        )

        val longBow = RangedWeapon(
            name = "Long Bow",
            damageRoll = { Die.d8.roll(1) },
            properties = listOf(),
            range = 3,
        )

    }

    val name: String
    val damageRoll: () -> Int
    val properties: List<Property>

    fun isFinesse(): Boolean {
        return properties.contains(Property.finesse)
    }

    data class MeleeWeapon(
        override val name: String,
        override val damageRoll: () -> Int,
        override val properties: List<Property>
    ): Weapon

    data class RangedWeapon(
        override val name: String,
        override val damageRoll: () -> Int,
        override val properties: List<Property>,
        val range: Int
    ): Weapon

    enum class Property {
        twoHanded, light, finesse
    }

}