package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Target
import com.manikoske.guild.character.CharacterState


data class PointOfView(
    private val taker: CharacterState,
    private val others: List<CharacterState>,
) {
    private val allies = others.filter { it.allegiance == taker.allegiance }
    private val enemies = others.filter { it.allegiance != taker.allegiance }

    fun allVantageNodes(battleground: Battleground): List<VantageNode> {

        val requiredNodeNormalMovements = battleground.getAllNodeNormalMovementRequirements(
            startNodeId = taker.positionNodeId,
        )

        val requiredNodeSpecialMovements = battleground.getAllNodeSpecialMovementRequirements(
            startNodeId = taker.positionNodeId,
        )

        return battleground.allBattlegroundNodes().map { node ->
            VantageNode(
                nodeId = node.id,
                requiredNormalMovement = requiredNodeNormalMovements.getValue(node.id),
                requiredSpecialMovement = requiredNodeSpecialMovements.getOrDefault(node.id, Int.MAX_VALUE),
                targets = node.lineOfSight.flatMap { targetsFrom(it)}
            )
        }
    }

    private fun targetsFrom(lineOfSight: Battleground.LineOfSight): List<Target> {
        val enemiesAtNode = enemies.filter { it.positionNodeId == lineOfSight.toNodeId }
        val alliesAtNode = allies.filter { it.positionNodeId == lineOfSight.toNodeId }

        val singleEnemies = enemiesAtNode.filter { it.targetableBy(Target.Type.SingleEnemy) }
        val singleAllies = alliesAtNode.filter { it.targetableBy(Target.Type.SingleAlly) }
        val nodeEnemies = enemiesAtNode.filter { it.targetableBy(Target.Type.NodeEnemy) }
        val nodeAllies = alliesAtNode.filter { it.targetableBy(Target.Type.NodeAlly) }
        val nodeEveryone = (enemiesAtNode + alliesAtNode).filter { it.targetableBy(Target.Type.NodeEveryone) }

        return buildList {
            singleEnemies.forEach {
                add(Target(type = Target.Type.SingleEnemy, range = lineOfSight.range, targetedCharacterStates = listOf(it)))
            }
            singleAllies.forEach {
                add(Target(type = Target.Type.SingleAlly, range = lineOfSight.range, targetedCharacterStates = listOf(it)))
            }
            if (nodeEnemies.isNotEmpty()) {
                add(Target(type = Target.Type.NodeEnemy, range = lineOfSight.range, targetedCharacterStates = nodeEnemies))
            }
            if (nodeAllies.isNotEmpty()) {
                add(Target(type = Target.Type.NodeAlly, range = lineOfSight.range, targetedCharacterStates = nodeAllies))
            }
            if (nodeEveryone.isNotEmpty()) {
                add(Target(type = Target.Type.NodeEveryone, range = lineOfSight.range, targetedCharacterStates = nodeEveryone))
            }
        }
    }

    data class VantageNode(
        val nodeId: Int,
        val requiredNormalMovement: Int,
        val requiredSpecialMovement: Int,
        val targets: List<Target>,
    ) {
         fun canBeAccessedWith(movement: Movement) : Boolean {
             return when (movement.type) {
                 Movement.Type.Normal -> movement.amount >= requiredNormalMovement
                 Movement.Type.Special -> movement.amount >= requiredSpecialMovement
             }
         }
     }

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