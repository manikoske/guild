package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Effect
import com.manikoske.guild.character.Character
import kotlin.math.max

class CharacterState(
    val character: Character,
    var positionNodeId: Int,
    var allegiance: Allegiance,
    private var damageTaken: Int,
    private var resourcesSpent: Int,
    private var actionForcingEffect: Effect.ActionForcingEffect?,
    private var movementRestrictingEffect: Effect.MovementRestrictingEffect?,
    private var movementAlteringEffects: List<Effect.MovementAlteringEffect>,
    private var actionRestrictingEffects: List<Effect.ActionRestrictingEffect>,
    private var damageOverTimeEffects: List<Effect.DamageOverTimeEffect>,
    private var healOverTimeEffects: List<Effect.HealOverTimeEffect>,
) {

    fun copy(): CharacterState {
        return CharacterState(
            character = character,
            positionNodeId = positionNodeId,
            allegiance = allegiance,
            damageTaken = damageTaken,
            resourcesSpent = resourcesSpent,
            actionForcingEffect = actionForcingEffect,
            movementRestrictingEffect = movementRestrictingEffect,
            movementAlteringEffects = movementAlteringEffects.toList(),
            actionRestrictingEffects = actionRestrictingEffects.toList(),
            damageOverTimeEffects = damageOverTimeEffects.toList(),
            healOverTimeEffects = healOverTimeEffects.toList()
        )
    }

    fun takeDamage(hitPointDamage: Int) {
        this.damageTaken = max(0, damageTaken + hitPointDamage)
        if (damageTaken >= character.maxHitPoints()) {
            addEffect(Effect.ActionForcingEffect.Dying)
        }
        removeEffectsOnDamage()
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

    fun addEffect(effect: Effect) {
        when (effect) {
            is Effect.ActionForcingEffect -> actionForcingEffect = effect.add(actionForcingEffect)
            is Effect.MovementRestrictingEffect -> movementRestrictingEffect = effect.add(movementRestrictingEffect)
            is Effect.ActionRestrictingEffect -> actionRestrictingEffects = effect.add(actionRestrictingEffects)
            is Effect.DamageOverTimeEffect -> damageOverTimeEffects = effect.add(damageOverTimeEffects)
            is Effect.MovementAlteringEffect -> movementAlteringEffects = effect.add(movementAlteringEffects)
            is Effect.HealOverTimeEffect -> healOverTimeEffects = effect.add(healOverTimeEffects)
        }
    }


    fun removeEffect(effect: Effect) {
        when (effect) {
            is Effect.ActionForcingEffect -> actionForcingEffect = effect.remove(actionForcingEffect)
            is Effect.MovementRestrictingEffect -> movementRestrictingEffect = effect.remove(movementRestrictingEffect)
            is Effect.ActionRestrictingEffect -> actionRestrictingEffects = effect.remove(actionRestrictingEffects)
            is Effect.DamageOverTimeEffect -> damageOverTimeEffects = effect.remove(damageOverTimeEffects)
            is Effect.MovementAlteringEffect -> movementAlteringEffects = effect.remove(movementAlteringEffects)
            is Effect.HealOverTimeEffect -> healOverTimeEffects = effect.remove(healOverTimeEffects)
        }
    }

    private fun removeEffectsOnDamage() {
        allEffects().filter { it.removeOnDamageTaken() }.forEach { removeEffect(it) }
    }

    fun decrementRoundsLeft() {
        allEffects().forEach { effect ->
            when (val roundState = effect.tick()) {
                Effect.RoundState.Expired -> removeEffect(effect)
                is Effect.RoundState.Timed -> addEffect(roundState.nextRoundEffect)
                Effect.RoundState.Untimed -> Unit
            }
        }
    }

    private fun allEffects(): List<Effect> {
        return (
                movementAlteringEffects +
                        actionRestrictingEffects +
                        damageOverTimeEffects +
                        healOverTimeEffects +
                        listOf(actionForcingEffect) +
                        listOf(movementRestrictingEffect)
                )
            .filterNotNull()
    }

    fun moveTo(newPositionNodeIde: Int) {
        this.positionNodeId = newPositionNodeIde
    }

    fun applyOverTimeEffects() {
        heal(healOverTimeEffects.sumOf { it.healRoll().invoke() })
        takeDamage(damageOverTimeEffects.sumOf { it.damageRoll().invoke() })
    }

    fun canExecuteAction(eventualAction: Action): Boolean {
        val noActionRestrictionEffect = actionRestrictingEffects.none { it.restrictedAction(eventualAction) }
        val classRestriction = eventualAction.classRestriction.contains(character.clazz())
        val resourceRestriction = eventualAction.resourceCost < character.maxResources() - resourcesSpent
        val armsRestriction = eventualAction.armsRestriction.invoke(character.arms())
        return noActionRestrictionEffect && classRestriction && resourceRestriction && armsRestriction
    }

    fun forcedToAction(): Action.ForcedAction? {
        return actionForcingEffect?.forcedAction()
    }

    fun canMoveBy(actionMovement: Movement): Movement {
        val finalMovement = (movementRestrictingEffect?.restrictActionMovement(actionMovement) ?: actionMovement)
            .let { if (it.amount > 0) movementAlteringEffects.fold(it) { a, b -> b.alterActionMovement(a) } else it }

        return finalMovement
    }

    enum class Allegiance {
        Attacker, Defender
    }
}
