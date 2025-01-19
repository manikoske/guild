package com.manikoske.guild.action

data class Movement(val type: Type, val amount: Int) {

    enum class Type {
        Normal, Special
    }
}
