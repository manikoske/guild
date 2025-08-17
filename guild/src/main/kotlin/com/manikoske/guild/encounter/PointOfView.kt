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