package com.manikoske.guild.ability

data class TargetType(val scope: Scope, val range: Int, val arity: Arity) {

    enum class Arity {
        Single, Double, Triple, Node, Battleground
    }

    enum class Scope {
        Ally, Enemy, Self, Everyone
    }

}