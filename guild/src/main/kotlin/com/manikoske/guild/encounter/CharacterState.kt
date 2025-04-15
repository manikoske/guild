package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Effect
import com.manikoske.guild.character.Character
import java.util.logging.Logger
import kotlin.math.max
import kotlin.math.min

data class CharacterState(
    val character: Character,
    val positionNodeId: Int,
    val allegiance: Allegiance,
    private val damageTaken: Int,
    private val resourcesSpent: Int,
    private val effects: Effects,
) {

    companion object {

        val LOG: Logger = Logger.getLogger(Encounter::class.java.name)

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

    fun isDying() : Boolean {
        return effects.actionForcingEffect is Effect.ActionForcingEffect.Dying
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

    private fun currentHitPoints() : Int {
        return character.maxHitPoints() - damageTaken
    }

    private fun currentResources() : Int {
        return character.maxResources() - resourcesSpent
    }

    private fun resolveEffectsOnDamage() : CharacterState {
        return this.copy(
            effects = (if (currentHitPoints() == 0) effects.add(Effect.ActionForcingEffect.Dying(0)) else effects).removeOnDamage()
        )
    }

    fun takeDamage(damageToTake: Int) : CharacterState {
        if (damageToTake == 0) return this
        val damageTakenUpdated = min(character.maxHitPoints(), damageTaken + damageToTake)
        return this.copy(damageTaken = damageTakenUpdated).resolveEffectsOnDamage()
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
        if (amountToHeal == 0) return this
        return this.copy(damageTaken = max(0, damageTaken - amountToHeal))
    }

    fun spendResources(amount: Int) : CharacterState {
        if (amount == 0) return this
        return this.copy(resourcesSpent = min(character.maxResources(), resourcesSpent + amount))
    }

    fun gainResources(amount: Int) : CharacterState {
        if (amount == 0) return this
        return this.copy(resourcesSpent = max(0, resourcesSpent - amount))
    }

    fun moveTo(newPositionNodeIde: Int) : CharacterState {
        return this.copy(positionNodeId = newPositionNodeIde)
    }

    fun applyOverTimeEffects() : CharacterState {
        return heal(effects.healOverTimeEffects.sumOf { it.heal().roll() })
            .takeDamage(effects.damageOverTimeEffects.sumOf { it.damage().roll() })
    }

    fun allExecutableActions() : List<Action> {
        return effects.actionForcingEffect?.forcedAction().let { forcedAction ->
            if (forcedAction == null) {
                Action.Actions.basicActions.filter { canExecuteAction(it) }
            } else {
                listOf(forcedAction)
            }
        }
    }

    fun canMoveBy(actionMovement: Movement): Movement {
        val finalMovement = (effects.movementRestrictingEffect?.restrictActionMovement(actionMovement) ?: actionMovement)
            .let { if (it.amount > 0) effects.movementAlteringEffects.fold(it) { a, b -> b.alterActionMovement(a) } else it }

        return finalMovement
    }

    private fun canExecuteAction(eventualAction: Action): Boolean {
        val noActionRestrictionEffect = effects.actionRestrictingEffects.none { it.restrictedAction(eventualAction) }
        val classRestriction = eventualAction.classRestriction.contains(character.clazz())
        val resourceRestriction = eventualAction.resourceCost == 0 || eventualAction.resourceCost < currentResources()
        return noActionRestrictionEffect && classRestriction && resourceRestriction
    }

    enum class Allegiance {
        Attacker, Defender
    }

    fun print() : String {
        return buildString {
            appendLine("----- Character State -----")
            appendLine("Character ID: ${character.id}")
            appendLine("Name: ${character.bio.name}")
            appendLine("Allegiance: $allegiance")
            appendLine("Position Node ID: $positionNodeId")
            appendLine("Hit Points: ${currentHitPoints()} / ${character.maxHitPoints()}")
            appendLine("Resources: ${currentResources()} / ${character.maxResources()}")
            appendLine("Utility: ${utility()}")
            appendLine("Active Effects:")
            val activeEffects = effects.all()
            if (activeEffects.isEmpty()) {
                appendLine("  None")
            } else {
                activeEffects.forEach { effect ->
                    appendLine("  - [${effect.category}] $effect")
                }
            }
            appendLine("---------------------------")
        }
    }
}
