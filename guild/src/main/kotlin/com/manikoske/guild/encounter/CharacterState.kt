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
    var statuses: MutableList<Status>, // TODO how does it behave after context copy?
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
        this.statuses.add = this.statuses.map { if (it.category == status.category) status else it }
    }

    fun removeStatus(status: Status) {
        this.statuses = this.statuses.filter { it is status.class }
    }

    fun moveTo(newPositionNodeIde: Int) {
        this.positionNodeId = newPositionNodeIde
    }

    fun applyOverTimeStatuses() {
        heal(statuses.filterIsInstance<Status.HealOverTimeStatus>().sumOf { it.healRoll().invoke() })
        takeDamage(statuses.filterIsInstance<Status.DamageOverTimeStatus>().sumOf { it.damageRoll().invoke() })
    }

    fun canExecuteAction(eventualAction: Action): Boolean {
        val noActionRestrictionStatus = statuses.filterIsInstance<Status.ActionRestrictingStatus>().none { it.restrictedAction(eventualAction)}
        val classRestriction = eventualAction.classRestriction.contains(character.clazz())
        val resourceRestriction = eventualAction.resourceCost < character.maxResources() - resourcesSpent
        val armsRestriction = eventualAction.armsRestriction.invoke(character.arms())
        return noActionRestrictionStatus && classRestriction && resourceRestriction && armsRestriction
    }

    fun forcedToAction() : Action.ForcedAction? {
        return statuses.filterIsInstance<Action.ForcedAction>().firstOrNull()
    }

    fun canMoveBy(actionMovement: Movement): Movement {
        val movementProhibitingStatus = statuses.filterIsInstance<Status.MovementRestrictingStatus>().firstOrNull()
        val movementAlteringStatuses = statuses.filterIsInstance<Status.MovementAlteringStatus>()

        val finalMovement = (movementProhibitingStatus?.restrictActionMovement(actionMovement) ?: actionMovement)
            .let { if (it.amount > 0) movementAlteringStatuses.fold(it) { a, b -> b.alterActionMovement(a) } else it }

        return finalMovement
    }

    enum class Allegiance {
        Attacker, Defender
    }
}
