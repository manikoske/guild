package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
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

    fun applyEffect(status: Status) {
        this.statuses += status
    }

    fun moveTo(newPositionNodeIde: Int) {
        this.positionNodeId = newPositionNodeIde
    }

    fun canExecuteAction(executableAction: Action): Boolean {
        val classRestriction = executableAction.classRestriction.contains(character.clazz())
        val resourceRestriction = executableAction.resourceCost < character.maxResources() - resourcesSpent
        val armsRestriction = executableAction.armsRestriction.invoke(character.arms())
        return classRestriction && resourceRestriction && armsRestriction
    }

    enum class Allegiance {
        Attacker, Defender
    }
}
