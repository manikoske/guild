package com.manikoske.guild.action

data class Movement(val type: Type, val nodes: Int) {

    enum class Type {
        Normal, Special
    }
}
