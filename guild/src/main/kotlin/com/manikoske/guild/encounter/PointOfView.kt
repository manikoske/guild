package com.manikoske.guild.encounter


data class PointOfView(
    private val takerId: Int,
    val characterStates: List<CharacterState>,
) {
    val taker = characterStates.first {it.character.id == takerId}
    val allies = characterStates.filter { it.character.id != takerId && it.allegiance == taker.allegiance }
    val enemies = characterStates.filter { it.allegiance != taker.allegiance }

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

    fun updateWith(updatedCharacterStates: List<CharacterState>) : PointOfView {
        return PointOfView(
            takerId = takerId,
            characterStates = (updatedCharacterStates + characterStates).distinctBy { it.character.id }
        )
    }

    fun utility(): Double {
        return allies.sumOf { it.utility() } - enemies.sumOf { it.utility() }
    }

}