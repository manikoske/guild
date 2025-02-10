package com.manikoske.guild.encounter

import com.manikoske.guild.action.*
import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.Character
import kotlin.random.Random

data class EncounterState(
    private val characterStates: Map<Int, CharacterState>,
) {
    fun utility(): Int {
        return Random.nextInt(1, 10)
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

        // TODO accumulate updated characterStates

        val ending = this.copy()
        val executor = ending.characterState(executorCharacterId)

        executor.moveTo(newPositionNodeId)
        executor.spendResources(action.resourceCost)

        actionTargets.forEach { actionTarget ->
            val target = ending.characterState(actionTarget)

            if (resolveOutcome(action.outcome(executor.character), executor, target)) {
                when (val triggeredAction = action.triggeredAction) {

                    is TriggeredAction.SelfTriggeredAction -> resolveOutcome(
                        outcome = triggeredAction.outcome,
                        executor = executor,
                        target = executor
                    )

                    is TriggeredAction.TargetTriggeredAction -> resolveOutcome(
                        outcome = triggeredAction.outcome,
                        executor = executor,
                        target = target
                    )

                    null -> Unit
                }
            }
        }
        executor.applyOverTimeEffects()
        executor.tickEffects()
        return ending
    }

    // TODO move to outcome
    private fun resolveOutcome(
        outcome: Outcome,
        executor: CharacterState,
        target: CharacterState,
    ): Boolean {
        if (!outcome.savingThrow.saved(executor.character, target.character)) {
            when (outcome) {
                is Outcome.AvoidableDamage ->
                    target.takeDamage(
                        executor.character.attributeRoll(
                            outcome.executorAttributeType,
                            outcome.damageRoll
                        )
                    )


                is Outcome.WeaponDamage ->
                    target.takeDamage(
                        executor.character.weaponDamageRoll(
                            outcome.damageRoll,
                            outcome.damageRollMultiplier
                        )
                    )

                is Outcome.RemoveEffect ->
                    target.removeEffect(outcome.effect)
            }
            return true
        } else {
            return false
        }
    }

    fun eventualActionTargets(
        pointOfView: PointOfView,
        vantageNode: PointOfView.VantageNode,
        action: Action
    ): List<List<Int>> {

        // TODO when range > 0 and enemycount from vangate node > 1 then disadvantage
        val result: MutableList<List<Int>> = mutableListOf()

        val targetType = action.targetType(character(pointOfView.self))

        for (eventualActionTargetNodeId in vantageNode.targetNodes.filter { it.range <= targetType.range }) {

            val scopedTargets = when (targetType.scope) {
                TargetType.Scope.Ally -> pointOfView.allies
                TargetType.Scope.Enemy -> pointOfView.enemies
                TargetType.Scope.Self -> listOf(pointOfView.self)
                TargetType.Scope.Everyone -> pointOfView.everyone
                TargetType.Scope.EveryoneElse -> pointOfView.everyoneElse
            }

            val scopedTargetsAtPossibleTargetNode =
                scopedTargets.intersect(eventualActionTargetNodeId.characterIds.toSet()).toList()

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

    fun allAccessibleVantageNodes(pointOfView: PointOfView, actionMovement: Movement): List<PointOfView.VantageNode> {
        val characterMovement = characterState(pointOfView.self).canMoveBy(actionMovement)
        return pointOfView.vantageNodes.filter {
            when (characterMovement.type) {
                Movement.Type.Normal -> it.requiredNormalMovement <= characterMovement.amount
                Movement.Type.Special -> it.requiredSpecialMovement <= characterMovement.amount
            }
        }
    }

    fun allEventualActions(pointOfView: PointOfView): List<Action> {
        return characterState(pointOfView.self).let { self ->
            self.forcedToAction().let { forcedAction ->
                if (forcedAction == null) {
                    Action.Actions.actions.filter { self.canExecuteAction(it) }
                } else {
                    listOf(forcedAction)
                }
            }
        }
    }


    fun viewFrom(
        characterId: Int,
        battleground: Battleground
    ): PointOfView {
        val self = characterStates.getValue(characterId)
        val allies = characterStates.values.filter { it.allegiance == self.allegiance }
        val enemies = characterStates.values.filter { it.allegiance != self.allegiance }
        val everyoneElse = characterStates.values.filter { it.character.id != characterId }
        val everyone = characterStates.values.toList()

        val allyCountPerNode = characterCountPerNode(allies)
        val enemyCountPerNode = characterCountPerNode(enemies)

        val requiredNodeNormalMovements = battleground.getAllNodeNormalMovementRequirements(
            startNodeId = self.positionNodeId,
            allyCountPerNode = allyCountPerNode,
            enemyCountPerNode = enemyCountPerNode,
        )

        val requiredNodeSpecialMovements = battleground.getAllNodeSpecialMovementRequirements(
            startNodeId = self.positionNodeId,
            allyCountPerNode = allyCountPerNode,
            enemyCountPerNode = enemyCountPerNode
        )

        return PointOfView(
            self = characterId,
            enemies = enemies.map { it.character.id },
            allies = allies.map { it.character.id },
            everyone = everyone.map { it.character.id },
            everyoneElse = everyoneElse.map { it.character.id },
            vantageNodes = battleground.allBattlegroundNodes().map { node ->
                PointOfView.VantageNode(
                    nodeId = node.id,
                    hasEnemiesPresent = enemyCountPerNode.getOrDefault(node.id, 0) > 0,
                    requiredNormalMovement = requiredNodeNormalMovements.getValue(node.id),
                    requiredSpecialMovement = requiredNodeSpecialMovements.getOrDefault(node.id, Int.MAX_VALUE),
                    targetNodes = node.lineOfSight.map {
                        PointOfView.TargetNode(
                            nodeId = it.toNodeId,
                            characterIds = charactersAt(it.toNodeId),
                            range = it.range
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
                for (j in i + 1..<targets.size) {
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
                for (j in i + 1..<targets.size) {
                    for (k in j + 1..<targets.size) {
                        result.add(listOf(targets[i], targets[j], targets[k]))
                    }
                }
            }
        }
        return result
    }

}
