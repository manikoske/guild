package com.manikoske.guild.character

import com.manikoske.guild.rules.Attribute
import com.manikoske.guild.rules.Class

data class Innate(
    // bio
    val name: String,
    // attributes
    val strength: Attribute,
    val dexterity: Attribute,
    val constitution: Attribute,
    val wisdom: Attribute,
    val intelligence: Attribute,
    val charisma: Attribute,
    // class
    val clazz: Class
) {
}