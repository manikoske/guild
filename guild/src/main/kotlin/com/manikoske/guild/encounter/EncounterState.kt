package com.manikoske.guild.encounter

import com.manikoske.guild.action.*
import com.manikoske.guild.character.Attribute

data class EncounterState(
    private val characterStates: Map<Int, CharacterState>,
) {
    fun utility(): Int {
        return 1
    }

    private fun copy(): EncounterState {
        return EncounterState(this.characterStates.values.associateBy({ it.character.id }, { it.copy() }))
    }

    fun resolveEnding(
        executorCharacterId: Int,
        newPositionNodeId: Int,
        action: Action,
        targets: List<Int>
    ): EncounterState {

        val possibleEnding = this.copy()
        val self = possibleEnding.characterState(executorCharacterId)

        self.moveTo(newPositionNodeId)
        self.spendResources(action.resourceCost)

        targets.forEach { target ->
            action.effects(self.character).forEach { effect ->
                val targetState = possibleEnding.characterState(target)
                if (resolveEffect(effect, self, targetState)) {
                    when (val triggeredAction = action.triggeredAction) {

                        is TriggeredAction.SelfTriggeredAction -> resolveEffect(
                            effect = triggeredAction.effect,
                            executorState = self,
                            targetState = self
                        )
                        is TriggeredAction.TargetTriggeredAction -> resolveEffect(
                            effect = triggeredAction.effect,
                            executorState = self,
                            targetState = targetState
                        )
                        null -> Unit
                    }
                }
            }
        }
        return possibleEnding
    }

    private fun resolveEffect(
        effect: Effect,
        executorState: CharacterState,
        targetState: CharacterState,
    ): Boolean {
        if (!effect.savingThrow.saved(executorState.character, targetState.character)) {
            when (effect) {
                is Effect.ApplyBuffStatus ->
                    targetState.applyEffect(effect.status)

                is Effect.ApplyStatus ->
                    targetState.applyEffect(effect.status)

                is Effect.AvoidableDamage ->
                    targetState.takeDamage(
                        executorState.character.attributeRoll(
                            effect.executorAttributeType,
                            effect.damageRoll
                        )
                    )

                is Effect.DirectDamage ->
                    targetState.takeDamage(effect.damageRoll.invoke())

                is Effect.Healing ->
                    targetState.heal(
                        executorState.character.attributeRoll(
                            Attribute.Type.wisdom,
                            effect.healingRoll
                        )
                    )

                Effect.NoEffect -> Unit
                is Effect.ResourceBoost ->
                    targetState.gainResources(effect.amount)

                is Effect.WeaponDamage ->
                    targetState.takeDamage(
                        executorState.character.weaponDamageRoll(
                            effect.damageRoll,
                            effect.damageRollMultiplier
                        )
                    )
            }
            return true
        } else {
            return false
        }
    }

    fun possibleTargets(pointOfView: PointOfView, vantageNode: PointOfView.VantageNode, targetType : TargetType) : List<List<Int>> {
        val result: MutableList<List<Int>> = mutableListOf()

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

    fun allAccessibleVantageNodes(actionMovement : Movement) : List<PointOfView.VantageNode> {
        return if (actionMovement.type == Movement.Type.Normal) {
            vantageNodes.filter { it.requiredNormalMovement <= actionMovement.nodes }
        } else {
            vantageNodes.filter { it.requiredSpecialMovement <= actionMovement.nodes }
        }
    }

    fun viewFrom(
        characterId: Int,
        battleground: Battleground
    ) : PointOfView {
        val self = characterStates.getValue(characterId)
        val allies = characterStates.values.filter { it.allegiance == self.allegiance }
        val enemies = characterStates.values.filter { it.allegiance != self.allegiance }
        val everyoneElse = characterStates.values.filter { it.character.id != self.character.id }
        val everyone = characterStates.values.toList()

        val allyCountPerNode = characterCountPerNode(allies)
        val enemyCountPerNode = characterCountPerNode(enemies)

        val requiredNodeNormalMovements = battleground.getAllNodeMovementRequirements(
            startNodeId = self.positionNodeId,
            allyCountPerNode = allyCountPerNode,
            enemyCountPerNode = enemyCountPerNode,
            movementType = Movement.Type.Normal
        )

        val requiredNodeSpecialMovements = battleground.getAllNodeMovementRequirements(
            startNodeId = self.positionNodeId,
            allyCountPerNode = allyCountPerNode,
            enemyCountPerNode = enemyCountPerNode,
            movementType = Movement.Type.Special
        )

        return PointOfView(
            self = self,
            enemies = enemies.map { it.character.id },
            allies = allies.map { it.character.id },
            everyone = everyone.map { it.character.id },
            everyoneElse = everyoneElse.map { it.character.id },
            vantageNodes = requiredNodeNormalMovements.keys.map { nodeId ->
                PointOfView.VantageNode(
                    nodeId = nodeId,
                    requiredNormalMovement = requiredNodeNormalMovements.getValue(nodeId),
                    requiredSpecialMovement = requiredNodeSpecialMovements.getValue(nodeId),
                    targetNodes = battleground.nodeIdsInRange(nodeId).map {
                        PointOfView.TargetNode(
                            nodeId = it.key,
                            characterIds = charactersAt(it.key),
                            range = it.value
                        )
                    }
                )
            }
        )
    }

    private fun characterCountPerNode(characterStates: List<CharacterState>): Map<Int, Int> {
        return characterStates.groupingBy { it.positionNodeId }.eachCount()
    }

    private fun charactersAt(nodeId: Int): List<Int> {
        return characterStates.filterValues { it.positionNodeId == nodeId }.values.map { it.character.id }
    }

    private fun characterState(characterId: Int): CharacterState {
        return characterStates.getValue(characterId)
    }

    private fun singleTarget(targets: List<Int>): List<List<Int>> {
        return targets.chunked(1)
    }

    private fun doubleTarget(targets: List<Int>): List<List<Int>> {
        val result: MutableList<List<Int>> = mutableListOf()
        if (targets.isNotEmpty()) {
            for (i in targets.indices) {
                for (j in i+1..<targets.size) {
                    result.add(listOf(targets[i], targets[j]))
                }
            }
        }
        return result
    }

    private fun tripleTarget(targets: List<Int>): List<List<Int>> {
        val result: MutableList<List<Int>> = mutableListOf()
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
