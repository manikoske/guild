package com.manikoske.guild.character

import com.manikoske.guild.inventory.Armor
import com.manikoske.guild.rules.Die

enum class Class(val hpDie: Die, val baseResources: Int, val armorProficiencies: List<Armor.Type>) {

    Fighter(Die.d12, 5, listOf(Armor.Type.cloth, Armor.Type.light, Armor.Type.medium, Armor.Type.heavy)),
    Rogue(Die.d6, 6, listOf(Armor.Type.cloth, Armor.Type.light)),
    Ranger(Die.d8, 3, listOf(Armor.Type.cloth, Armor.Type.light, Armor.Type.medium)),
    Cleric(Die.d10, 5, listOf(Armor.Type.cloth, Armor.Type.light, Armor.Type.medium, Armor.Type.heavy)),
    Wizard(Die.d6, 4, listOf(Armor.Type.cloth))

}