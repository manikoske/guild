package com.manikoske.guild.encounter

data class PointOfView(
    val taker: CharacterState,
    val allies: List<CharacterState>,
    val enemies: List<CharacterState>,
) {

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
}
