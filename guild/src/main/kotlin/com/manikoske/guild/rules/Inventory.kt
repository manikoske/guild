package com.manikoske.guild.rules

data class Inventory(
    val armor: Armor,
    val meleeMainHand: Weapon.MeleeWeapon?,
    val meleeOffHand: Weapon.OffHandMeleeWeapon?,
    val rangedWeapon: Weapon.RangedWeapon?
)
