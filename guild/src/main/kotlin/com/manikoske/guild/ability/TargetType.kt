package com.manikoske.guild.ability

data class TargetType(val range: Int, val arity: Arity) {

    enum class Arity {
        Self, Single, Double, Triple, Node
    }
}