package com.manikoske.guild.rules

data class Level(val level: Int) {

    fun modifier(): Int {
        return level / 2
    }
}