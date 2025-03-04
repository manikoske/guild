package com.manikoske.guild.character

data class Attribute(val score: Int, val type: Type) {

    enum class Type {
        strength,
        dexterity,
        constitution,
        wisdom,
        intelligence,
        charisma
    }

    fun modifier(): Int {
        return (score - 10) / 2
    }
}
