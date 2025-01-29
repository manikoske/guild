package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Status
import com.manikoske.guild.character.Character
import kotlin.math.max

data class CharacterState(
    val character: Character,
    var positionNodeId: Int,
    var allegiance: Allegiance,
    var damageTaken: Int,
    var resourcesSpent: Int,
    var statuses: List<Status>,
) {
    fun takeDamage(hitPointDamage: Int) {
        this.damageTaken = max(0, damageTaken + hitPointDamage)
        if (damageTaken >= character.maxHitPoints()) {
            addStatus(Status.Dying(tries = 3))
        }
        this.statuses = this.statuses.filter { it !is Status.RemovedOnDamageTakenStatus }
    }

    fun heal(hitPointHeal: Int) {
        this.damageTaken = max(0, damageTaken - hitPointHeal)
    }

    fun spendResources(amount: Int) {
        this.resourcesSpent = max(0, resourcesSpent + amount)
    }

    fun gainResources(amount: Int) {
        this.resourcesSpent = max(0, resourcesSpent - amount)
    }

    fun addStatus(status: Status) {
        this.statuses = this.statuses.map { if (it.category == status.category) status else it }
    }

    fun removeStatus(status: Status) {
        this.statuses = this.statuses.filter { it.category != status.category }
    }

    fun moveTo(newPositionNodeIde: Int) {
        this.positionNodeId = newPositionNodeIde
    }

    fun applyOverTimeStatuses() {
        heal(statuses.filterIsInstance<Status.HealOverTime>().sumOf { it.healRoll.invoke() })
        takeDamage(statuses.filterIsInstance<Status.DamageOverTimeStatus>().sumOf { it.damageRoll().invoke() })
    }

    fun canExecuteAction(eventualAction: Action): Boolean {
        val noStatusExecutionProhibition = statuses.filterIsInstance<Status.ActionLimitingStatus>().none { it.limitsActionExecution(eventualAction)}
        val classRestriction = eventualAction.classRestriction.contains(character.clazz())
        val resourceRestriction = eventualAction.resourceCost < character.maxResources() - resourcesSpent
        val armsRestriction = eventualAction.armsRestriction.invoke(character.arms())
        return noStatusExecutionProhibition && classRestriction && resourceRestriction && armsRestriction
    }

    fun canMoveBy(actionMovement: Movement): Movement {
        val movementProhibitingStatus = statuses.filterIsInstance<Status.MovementProhibitingStatus>().firstOrNull()
        val movementAlteringStatus = statuses.filterIsInstance<Status.MovementAlteringStatus>().firstOrNull()

        val finalMovement = (movementProhibitingStatus?.prohibitsActionMovement(actionMovement) ?: actionMovement)
            .let { if (it.amount > 0) movementAlteringStatus?.altersActionMovement(it) ?: it else it }

        return finalMovement
    }

    enum class Allegiance {
        Attacker, Defender
    }
}
