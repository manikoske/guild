package com.manikoske.guild.character

import com.manikoske.guild.inventory.Armor
import com.manikoske.guild.rules.Die

enum class Class(val hpDie: Die, val baseResources: Int, val armorProficiencies: List<Armor.Type>) {

    fighter(Die.d12, 5, listOf(Armor.Type.cloth, Armor.Type.light, Armor.Type.medium, Armor.Type.heavy)),
    rogue(Die.d6, 6, listOf(Armor.Type.cloth, Armor.Type.light)),
    ranger(Die.d8, 3, listOf(Armor.Type.cloth, Armor.Type.light, Armor.Type.medium)),
    cleric(Die.d10, 5, listOf(Armor.Type.cloth, Armor.Type.light, Armor.Type.medium, Armor.Type.heavy)),
    wizard(Die.d6, 4, listOf(Armor.Type.cloth))

}