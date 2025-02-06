package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Effect
import com.manikoske.guild.character.Character
import kotlin.math.max

data class CharacterState(
    val character: Character,
    val positionNodeId: Int,
    val allegiance: Allegiance,
    private val damageTaken: Int,
    private val resourcesSpent: Int,
    val effects: Effects,
) {

    object CharacterStates {
        fun initialCharacterState(
            character: Character,
            startingNodeId: Int,
            allegiance: Allegiance
        ) : CharacterState {
            return CharacterState(
                character = character,
                positionNodeId = startingNodeId,
                allegiance = allegiance,
                damageTaken = 0,
                resourcesSpent = 0,
                effects = noEffects()
            )
        }

        fun noEffects() : Effects {
            return Effects(
                actionForcingEffect = null,
                movementRestrictingEffect = null,
                movementAlteringEffects = listOf(),
                actionRestrictingEffects = listOf(),
                damageOverTimeEffects = listOf(),
                healOverTimeEffects = listOf()
            )
        }
    }


    fun takeDamage(damageToTake: Int) : CharacterState {
        val damageTakenUpdated = max(0, damageTaken + damageToTake)
        return this.copy(
            damageTaken = damageTakenUpdated,
            effects = (if (damageTakenUpdated > character.maxHitPoints()) effects.add(Effect.ActionForcingEffect.Dying) else effects).removeOnDamage()
        )
    }

    fun addEffect(effect: Effect) : CharacterState {
        return this.copy(effects = effects.add(effect))
    }

    fun removeEffect(effect: Effect) : CharacterState {
        return this.copy(effects = effects.remove(effect))
    }

    fun tickEffects() : CharacterState {
        return this.copy(effects = effects.tick())
    }

    fun heal(amountToHeal: Int) : CharacterState {
        return this.copy(damageTaken = max(0, damageTaken - amountToHeal))
    }

    fun spendResources(amount: Int) : CharacterState {
        return this.copy(resourcesSpent = max(0, resourcesSpent + amount))
    }

    fun gainResources(amount: Int) : CharacterState {
        return this.copy(resourcesSpent = max(0, resourcesSpent - amount))
    }

    fun moveTo(newPositionNodeIde: Int) : CharacterState {
        return this.copy(positionNodeId = newPositionNodeIde)
    }

    fun applyOverTimeEffects() {
        heal(effects.healOverTimeEffects.sumOf { it.healRoll().invoke() })
        takeDamage(effects.damageOverTimeEffects.sumOf { it.damageRoll().invoke() })
    }

    fun canExecuteAction(eventualAction: Action): Boolean {
        val noActionRestrictionEffect = effects.actionRestrictingEffects.none { it.restrictedAction(eventualAction) }
        val classRestriction = eventualAction.classRestriction.contains(character.clazz())
        val resourceRestriction = eventualAction.resourceCost < character.maxResources() - resourcesSpent
        val armsRestriction = eventualAction.armsRestriction.invoke(character.arms())
        return noActionRestrictionEffect && classRestriction && resourceRestriction && armsRestriction
    }

    fun forcedToAction(): Action.ForcedAction? {
        return effects.actionForcingEffect?.forcedAction()
    }

    fun canMoveBy(actionMovement: Movement): Movement {
        val finalMovement = (effects.movementRestrictingEffect?.restrictActionMovement(actionMovement) ?: actionMovement)
            .let { if (it.amount > 0) effects.movementAlteringEffects.fold(it) { a, b -> b.alterActionMovement(a) } else it }

        return finalMovement
    }

    enum class Allegiance {
        Attacker, Defender
    }
}
