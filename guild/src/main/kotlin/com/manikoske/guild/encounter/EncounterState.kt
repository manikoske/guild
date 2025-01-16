package com.manikoske.guild.encounter

import com.manikoske.guild.action.*
import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.Character

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
        actionTargets: List<Int>
    ): EncounterState {

        val ending = this.copy()
        val executor = ending.characterState(executorCharacterId)

        executor.moveTo(newPositionNodeId)
        executor.spendResources(action.resourceCost)

        actionTargets.forEach { actionTarget ->
            action.effects(executor.character).forEach { effect ->
                val target = ending.characterState(actionTarget)
                if (resolveEffect(effect, executor, target)) {
                    when (val triggeredAction = action.triggeredAction) {

                        is TriggeredAction.SelfTriggeredAction -> resolveEffect(
                            effect = triggeredAction.effect,
                            executor = executor,
                            target = executor
                        )
                        is TriggeredAction.TargetTriggeredAction -> resolveEffect(
                            effect = triggeredAction.effect,
                            executor = executor,
                            target = target
                        )
                        null -> Unit
                    }
                }
            }
        }
        return ending
    }

    private fun resolveEffect(
        effect: Effect,
        executor: CharacterState,
        target: CharacterState,
    ): Boolean {
        if (!effect.savingThrow.saved(executor.character, target.character)) {
            when (effect) {
                is Effect.ApplyBuffStatus ->
                    target.applyEffect(effect.status)

                is Effect.ApplyStatus ->
                    target.applyEffect(effect.status)

                is Effect.AvoidableDamage ->
                    target.takeDamage(
                        executor.character.attributeRoll(
                            effect.executorAttributeType,
                            effect.damageRoll
                        )
                    )

                is Effect.DirectDamage ->
                    target.takeDamage(effect.damageRoll.invoke())

                is Effect.Healing ->
                    target.heal(
                        executor.character.attributeRoll(
                            Attribute.Type.wisdom,
                            effect.healingRoll
                        )
                    )

                Effect.NoEffect -> Unit
                is Effect.ResourceBoost ->
                    target.gainResources(effect.amount)

                is Effect.WeaponDamage ->
                    target.takeDamage(
                        executor.character.weaponDamageRoll(
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

    fun eventualActionTargets(pointOfView: PointOfView, vantageNode: PointOfView.VantageNode, action: Action) : List<List<Int>> {

        val result: MutableList<List<Int>> = mutableListOf()

        val targetType = action.targetType(character(pointOfView.self))

        for (eventualActionTargetNodeId in vantageNode.targetNodes.filter { it.range <= targetType.range } ) {

            val scopedTargets = when (targetType.scope) {
                TargetType.Scope.Ally -> pointOfView.allies
                TargetType.Scope.Enemy -> pointOfView.enemies
                TargetType.Scope.Self -> listOf(pointOfView.self)
                TargetType.Scope.Everyone -> pointOfView.everyone
                TargetType.Scope.EveryoneElse -> pointOfView.everyoneElse
            }

            val scopedTargetsAtPossibleTargetNode = scopedTargets.union(eventualActionTargetNodeId.characterIds).toList()

            when (targetType.arity) {
                TargetType.Arity.Node -> result.add(scopedTargetsAtPossibleTargetNode)
                TargetType.Arity.Single -> result.addAll(singleTarget(scopedTargetsAtPossibleTargetNode))
                TargetType.Arity.Double -> result.addAll(doubleTarget(scopedTargetsAtPossibleTargetNode))
                TargetType.Arity.Triple -> result.addAll(tripleTarget(scopedTargetsAtPossibleTargetNode))
                TargetType.Arity.Battleground -> result.addAll(listOf(scopedTargets))
            }
        }
        return result
    }

    fun allAccessibleVantageNodes(pointOfView: PointOfView, actionMovement : Movement) : List<PointOfView.VantageNode> {
        return if (actionMovement.type == Movement.Type.Normal) {
            pointOfView.vantageNodes.filter { it.requiredNormalMovement <= actionMovement.nodes }
        } else {
            pointOfView.vantageNodes.filter { it.requiredSpecialMovement <= actionMovement.nodes }
        }
    }

    fun allEventualActions(pointOfView: PointOfView): List<Action> {
        return Action.Actions.actions.filter { characterState(pointOfView.self).canExecuteAction(it) }
    }

    fun viewFrom(
        characterId: Int,
        battleground: Battleground
    ) : PointOfView {
        val self = characterStates.getValue(characterId)
        val allies = characterStates.values.filter { it.allegiance == self.allegiance }
        val enemies = characterStates.values.filter { it.allegiance != self.allegiance }
        val everyoneElse = characterStates.values.filter { it.character.id != characterId }
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
            self = characterId,
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

    private fun character(characterId: Int): Character {
        return characterStates.getValue(characterId).character
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
