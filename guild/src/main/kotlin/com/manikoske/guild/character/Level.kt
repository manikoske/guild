package com.manikoske.guild.character

data class Level(val level: Int) {

    fun modifier(): Int {
        return level / 2
    }
}