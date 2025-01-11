package com.manikoske.guild.character

data class Bio(
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