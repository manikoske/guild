package com.manikoske.guild.encounter

import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.TargetType

data class PointOfView(
    val self: CharacterState,
    private val enemies: List<CharacterState>,
    private val allies: List<CharacterState>,
    private val everyone: List<CharacterState>,
    private val everyoneElse: List<CharacterState>,
    private val vantageNodes: List<VantageNode>,
) {

    data class VantageNode(
        val nodeId: Int,
        val requiredNormalMovement: Int,
        val requiredSpecialMovement: Int,
        val targetNodes: List<TargetNode>,
    )

    data class TargetNode(
        val nodeId: Int,
        val characterIds: List<Int>,
        val range: Int,

    )

    fun allPossibleMovements(actionMovement : Movement) : List<VantageNode> {
        return if (actionMovement.type == Movement.Type.Normal) {
            vantageNodes.filter { it.requiredNormalMovement <= actionMovement.nodes }
        } else {
            vantageNodes.filter { it.requiredSpecialMovement <= actionMovement.nodes }
        }
    }

    // TODO mozno cele premiestnit naspat do encountercontextu a tu nechat len gettre na priatelov etc. (cez idcka)
    fun possibleTargets(vantageNode: VantageNode, targetType : TargetType) : List<List<Int>> {
        val result: MutableList<List<CharacterState>> = mutableListOf()

        for (possibleTargetNodeId in vantageNode.targetNodes.filter { it.range <= targetType.range } ) {

            val scopedTargets = when (targetType.scope) {
                TargetType.Scope.Ally -> allies
                TargetType.Scope.Enemy -> enemies
                TargetType.Scope.Self -> listOf(self)
                TargetType.Scope.Everyone -> everyone
                TargetType.Scope.EveryoneElse -> everyoneElse
            }

            val scopedTargetsAtPossibleTargetNode = atNode(scopedTargets, possibleTargetNodeId)

            when (targetType.arity) {
                TargetType.Arity.Node -> result.add(scopedTargetsAtPossibleTargetNode)
                TargetType.Arity.Single -> result.addAll(singleTarget(scopedTargetsAtPossibleTargetNode))
                TargetType.Arity.Double -> result.addAll(doubleTarget(scopedTargetsAtPossibleTargetNode))
                TargetType.Arity.Triple -> result.addAll(tripleTarget(scopedTargetsAtPossibleTargetNode))
                TargetType.Arity.Battleground -> result.addAll(listOf(scopedTargets))
            }
        }
        return result.map { targets -> targets.map { target -> target.character.id } }

    }
    private fun singleTarget(targets: List<CharacterState>): List<List<CharacterState>> {
        return targets.chunked(1)
    }

    private fun doubleTarget(targets: List<CharacterState>): List<List<CharacterState>> {
        val result: MutableList<List<CharacterState>> = mutableListOf()
        if (targets.isNotEmpty()) {
            for (i in targets.indices) {
                for (j in i+1..<targets.size) {
                    result.add(listOf(targets[i], targets[j]))
                }
            }
        }
        return result
    }

    private fun tripleTarget(targets: List<CharacterState>): List<List<CharacterState>> {
        val result: MutableList<List<CharacterState>> = mutableListOf()
        if (targets.isNotEmpty()) {
            for (i in targets.indices) {
                for (j in i+1..<targets.size) {
                    for (k in j+1..<targets.size) {
                        result.add(listOf(targets[i], targets[j], targets[k]))
                    }
                }
            }
        }
        return result
    }

}
