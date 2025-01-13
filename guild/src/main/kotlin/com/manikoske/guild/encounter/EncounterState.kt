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

    private fun characterState(characterId: Int): CharacterState {
        return characterStates.getValue(characterId)
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
                            targetState = self)
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

    fun viewFrom(
        characterId: Int,
        battleground: Battleground
    ) : PointOfView {
        val self = characterStates.values.first { it.character.id == characterId }
        val allies = characterStates.values.filter { characterState -> characterState.allegiance == self.allegiance }
        val enemies = characterStates.values.filter { characterState -> characterState.allegiance != self.allegiance }
        val everyoneElse =
            characterStates.values.filter { characterState -> characterState.character.id != self.character.id }

        val allyCountPerNode = characterCountPerNode(allies)
        val enemyCountPerNode = characterCountPerNode(enemies)

        val requiredNodeNormalMovements = battleground.getRequiredNodeMovements(
            startNodeId = self.positionNodeId,
            allyCountPerNode = allyCountPerNode,
            enemyCountPerNode = enemyCountPerNode,
            movementType = Movement.Type.Normal
        )

        val requiredNodeSpecialMovements = battleground.getRequiredNodeMovements(
            startNodeId = self.positionNodeId,
            allyCountPerNode = allyCountPerNode,
            enemyCountPerNode = enemyCountPerNode,
            movementType = Movement.Type.Special
        )

        return PointOfView(
            self = self,
            enemies = enemies,
            allies = allies,
            everyone = characterStates.values.toList(),
            everyoneElse = everyoneElse,
            nodeMovementAndVision = requiredNodeNormalMovements.keys.map {
                PointOfView.MovementAndVision(
                    nodeId = it,
                    normalMovementRequired = requiredNodeNormalMovements.getValue(it),
                    specialMovementRequired = requiredNodeSpecialMovements.getValue(it),
                    nodeToRanges = battleground.nodeIdsInRange(it)
                )
            }
        )
    }

    private fun characterCountPerNode(characterStates: List<CharacterState>): Map<Int, Int> {
        return characterStates.groupingBy { it.positionNodeId }.eachCount()
    }

}
