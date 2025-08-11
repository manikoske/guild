package com.manikoske.guild.character

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.log.LoggingUtils
import kotlin.math.max
import kotlin.math.min

data class CharacterState(
    val character: Character,
    val positionNodeId: Int,
    val allegiance: Allegiance,
    private val damageTaken: Int,
    private val resourcesSpent: Int,
    val statuses: List<Status>,
) {

    override fun toString(): String {
        return LoggingUtils.formatCharacterState(this)
    }

    enum class Allegiance {
        Attacker, Defender
    }

    fun utility(): Double {
        val hitPointRatio = currentHitPoints() / character.maxHitPoints().toDouble()
        val resourceRatio = currentResources() / character.maxResources().toDouble()

        // Define weights: health is three times more important than resources.
        val healthWeight = 3.0
        val resourceWeight = 1.0
        val epsilon = 1e-6  // To avoid division by zero

        // Compute the weighted harmonic mean:
        // Weighted Harmonic Mean = (w1 + w2) / (w1/healthRatio + w2/resourceRatio)
        return if (hitPointRatio > epsilon && resourceRatio > epsilon) {
            (healthWeight + resourceWeight) / ((healthWeight / (hitPointRatio + epsilon)) + (resourceWeight / (resourceRatio + epsilon)))
        } else {
            0.0
        }
    }

    fun currentHitPoints(): Int {
        return character.maxHitPoints() - damageTaken
    }

    fun currentResources(): Int {
        return character.maxResources() - resourcesSpent
    }

    fun takeDamage(damageToTake: Int): CharacterState {
        return this.copy(damageTaken = min(character.maxHitPoints(), damageTaken + damageToTake))
    }

    //todo TICK? alebo len do endAction?

    fun addStatus(status: Status): CharacterState {
        return this.copy(statuses = statuses.map { if (it.name == status.name) status else it } )
    }

    fun removeStatuses(statusesToRemove: List<Status>): CharacterState {
        return this.copy(statuses = statuses - statusesToRemove)
    }

    fun heal(amountToHeal: Int): CharacterState {
        return this.copy(damageTaken = max(0, damageTaken - amountToHeal))
    }

    fun spendResources(amount: Int): CharacterState {
        return this.copy(resourcesSpent = min(character.maxResources(), resourcesSpent + amount))
    }

    fun gainResources(amount: Int): CharacterState {
        return this.copy(resourcesSpent = max(0, resourcesSpent - amount))
    }

    fun moveTo(newPositionNodeIde: Int): CharacterState {
        return this.copy(positionNodeId = newPositionNodeIde)
    }

    fun statusesToRemoveByCategory(category: Status.Category): List<Status> {
        return statuses.filter { it.category == category }
    }

    fun statusesToRemoveByDamage(): List<Status> {
        return statuses.filter { it.removedOnDamageTaken }
    }

    fun statusesToRemoveByMovement(): List<Status> {
        return statuses.filter { it.removedOnMovement }
    }

    private fun canExecuteAction(eventualAction: Action): Boolean {
        val noActionRestrictionEffect = effects.actionRestrictingEffects.none { it.restrictedAction(eventualAction) }
        val resourceRestriction = eventualAction.resourceCost == 0 || eventualAction.resourceCost <= currentResources()
        return noActionRestrictionEffect && resourceRestriction
    }

    fun allExecutableActions(): List<Action> {
        return effects.actionForcingEffect?.forcedAction().let { forcedAction ->
            if (forcedAction == null) {
                Action.Actions.basicActions.filter { canExecuteAction(it) }
            } else {
                listOf(forcedAction)
            }
        }
    }

    fun canMoveBy(actionMovement: Movement, requiredMovementAmount : Int): Boolean {
        val finalMovement =
            (effects.movementRestrictingEffect?.restrictActionMovement(actionMovement) ?: actionMovement)
                .let { if (it.amount > 0) effects.movementAlteringEffects.fold(it) { a, b -> b.alterActionMovement(a) } else it }

        return finalMovement.amount >= requiredMovementAmount
    }
}