package com.manikoske.guild.action

data class TargetType(val scope: Scope, val range: Int, val arity: Arity) {

    object TargetTypes {
        val self = TargetType(scope = Scope.Self, range = 0, arity = Arity.Single)
    }


    enum class Arity {
        Single, Double, Triple, Node, Battleground
    }

    enum class Scope {
        Ally, Enemy, Self, Everyone, EveryoneElse
    }

}