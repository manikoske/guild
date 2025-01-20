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
    fun takeDamage(hitPoints: Int) {
        this.damageTaken = max(0, damageTaken + hitPoints)
    }

    fun heal(hitPoints: Int) {
        this.damageTaken = max(0, damageTaken - hitPoints)
    }

    fun spendResources(amount: Int) {
        this.resourcesSpent = max(0, resourcesSpent + amount)
    }

    fun gainResources(amount: Int) {
        this.resourcesSpent = max(0, resourcesSpent - amount)
    }

    fun applyStatus(status: Status) {
        // TODO replace same effects with prolonged roundsleft
        this.statuses += status
    }

    fun removeStatus(status: Status) {
        this.statuses -= status
    }

    fun moveTo(newPositionNodeIde: Int) {
        this.positionNodeId = newPositionNodeIde
    }

    fun canExecuteAction(eventualAction: Action): Boolean {
        val noStatusExecutionProhibition = statuses.none { it.limitsActionExecution(eventualAction)}
        val classRestriction = eventualAction.classRestriction.contains(character.clazz())
        val resourceRestriction = eventualAction.resourceCost < character.maxResources() - resourcesSpent
        val armsRestriction = eventualAction.armsRestriction.invoke(character.arms())
        return noStatusExecutionProhibition && classRestriction && resourceRestriction && armsRestriction
    }

    fun canMoveBy(actionMovement: Movement): Movement {
        val movementProhibitingStatuses = statuses.filterIsInstance<Status.MovementProhibitingStatus>()
        val movementAlteringStatuses = statuses.filterIsInstance<Status.MovementAlteringStatus>()

        val finalMovement = movementProhibitingStatuses
            .fold(actionMovement){ a, b -> b.prohibitsActionMovement(a)}
            .let { if (it.amount > 0) movementAlteringStatuses.fold(it) { a, b -> b.altersActionMovement(a) } else it }

        return finalMovement
    }

    enum class Allegiance {
        Attacker, Defender
    }
}
