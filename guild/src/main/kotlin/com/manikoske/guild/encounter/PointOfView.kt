package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Target
import com.manikoske.guild.character.CharacterState


data class PointOfView(
    private val taker: CharacterState,
    private val others: List<CharacterState>,
) {
    private val allies = others.filter { it.allegiance == taker.allegiance }
    private val enemies = others.filter { it.allegiance != taker.allegiance }

    fun allVantageNodes(battleground: Battleground): List<VantageNode> {

        val allyCountPerNode = livingCharacterCountPerNode(allies + taker)
        val enemyCountPerNode = livingCharacterCountPerNode(enemies)

        val requiredNodeNormalMovements = battleground.getAllNodeNormalMovementRequirements(
            startNodeId = taker.positionNodeId,
            allyCountPerNode = allyCountPerNode,
            enemyCountPerNode = enemyCountPerNode,
        )

        val requiredNodeSpecialMovements = battleground.getAllNodeSpecialMovementRequirements(
            startNodeId = taker.positionNodeId,
            allyCountPerNode = allyCountPerNode,
            enemyCountPerNode = enemyCountPerNode
        )

        return battleground.allBattlegroundNodes().map { node ->
            VantageNode(
                nodeId = node.id,
                requiredNormalMovement = requiredNodeNormalMovements.getValue(node.id),
                requiredSpecialMovement = requiredNodeSpecialMovements.getOrDefault(node.id, Int.MAX_VALUE),
                targets = node.lineOfSight.map {
                    Target.Targets.possibleTargets(
                        range = it.range,
                        allies = allies.filter { ally -> ally.positionNodeId == it.toNodeId },
                        enemies = enemies.filter { enemy -> enemy.positionNodeId == it.toNodeId }
                    )
                }.flatten()
            )
        }
    }

    private fun livingCharacterCountPerNode(characterStates: List<CharacterState>): Map<Int, Int> {
        return characterStates.filter { !it.isDying() }.groupingBy { it.positionNodeId }.eachCount()
    }

    data class VantageNode(
        val nodeId: Int,
        val requiredNormalMovement: Int,
        val requiredSpecialMovement: Int,
        val targets: List<Target>,
    )

    fun updateWith(actionOutcome: Action.Outcome) : PointOfView {
        return if (actionOutcome is Action.TargetedActionOutcome) {
            PointOfView(
                taker = actionOutcome.actionEnded.updatedTarget,
                others = (actionOutcome.targetEvents.map { it.updatedTarget } + others).distinctBy { it.character.id },
            )
        } else {
            this.copy(taker = actionOutcome.actionEnded.updatedTarget)
        }
    }

    fun characterStates() : List<CharacterState> {
        return enemies + allies + taker
    }

    fun utility(): Double {
        return allies.sumOf { it.utility() } - enemies.sumOf { it.utility() }
    }

}