package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import kotlin.random.Random

data class EncounterState(
    private val characterStates: List<CharacterState>,
) {
    fun utility(): Int {
        return Random.nextInt(1, 10)
    }

    fun resolveEnding(
        takerCharacterId: Int,
        newPositionNodeId: Int,
        resourceCost: Int,
        updatedCharacterStates: List<CharacterState>
    ): EncounterState {

        val ending =
            updatedCharacterStates.fold(this) { encounterState, characterState -> encounterState.updateWith(characterState) }

        return ending.updateWith(
            ending.characterState(takerCharacterId)
                .moveTo(newPositionNodeId)
                .spendResources(resourceCost)
                .applyOverTimeEffects()
                .tickEffects()
        )
    }

    fun allAccessibleVantageNodes(pointOfView: PointOfView, actionMovement: Movement): List<PointOfView.VantageNode> {
        val characterMovement = pointOfView.taker.canMoveBy(actionMovement)
        return pointOfView.vantageNodes.filter {
            when (characterMovement.type) {
                Movement.Type.Normal -> it.requiredNormalMovement <= characterMovement.amount
                Movement.Type.Special -> it.requiredSpecialMovement <= characterMovement.amount
            }
        }
    }

    fun allEventualActions(executor : CharacterState): List<Action> {
        return executor.forcedToAction().let { forcedAction ->
                if (forcedAction == null) {
                    Action.Actions.basicActions.filter { executor.canExecuteAction(it) }
                } else {
                    listOf(forcedAction)
                }
            }
    }

    fun viewFrom(
        characterId: Int,
        battleground: Battleground
    ): PointOfView {
        val taker = characterState(characterId)
        val allies = characterStates.filter { it.character.id != characterId && it.allegiance == taker.allegiance }
        val enemies = characterStates.filter { it.allegiance != taker.allegiance }

        val allyCountPerNode = characterCountPerNode(allies + taker)
        val enemyCountPerNode = characterCountPerNode(enemies)

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

        return PointOfView(
            taker = taker,
            vantageNodes = battleground.allBattlegroundNodes().map { node ->
                PointOfView.VantageNode(
                    nodeId = node.id,
                    requiredNormalMovement = requiredNodeNormalMovements.getValue(node.id),
                    requiredSpecialMovement = requiredNodeSpecialMovements.getOrDefault(node.id, Int.MAX_VALUE),
                    targets = node.lineOfSight.map {
                            PointOfView.Target.Targets.possibleTargets(
                                range = it.range,
                                allies = allies.filter { ally -> ally.positionNodeId == it.toNodeId },
                                enemies = enemies.filter { enemy -> enemy.positionNodeId == it.toNodeId }
                        )
                    }.flatten() + PointOfView.Target.Self(self = taker)
                )
            }
        )
    }

    private fun characterCountPerNode(characterStates: List<CharacterState>): Map<Int, Int> {
        return characterStates.groupingBy { it.positionNodeId }.eachCount()
    }

    private fun characterState(characterId: Int): CharacterState {
        return characterStates.first { it.character.id == characterId }
    }

    private fun updateWith(newCharacterState: CharacterState) : EncounterState {
        return EncounterState(
            characterStates = characterStates.map { if (it.character.id == newCharacterState.character.id) newCharacterState else it }
        )
    }

}
