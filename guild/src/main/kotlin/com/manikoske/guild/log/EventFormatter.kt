package com.manikoske.guild.log

import com.manikoske.guild.action.Effect
import com.manikoske.guild.action.Event
import com.manikoske.guild.rules.Die

/**
 * Helper class for formatting events to text in a consistent way
 */
object EventFormatter {

    /**
     * Helper method to get the name of an effect category
     */
    private fun getCategoryName(category: Effect.Category): String {
        return when (category) {
            is Effect.ActionForcingEffect.Category,
            is Effect.ActionRestrictingEffect.Category,
            is Effect.MovementAlteringEffect.Category,
            is Effect.MovementRestrictingEffect.Category,
            is Effect.DamageOverTimeEffect.Category,
            is Effect.HealOverTimeEffect.Category -> category.toString()
            else -> "Unknown"
        }
    }

    /**
     * Returns a concise text description of an event
     */
    fun formatEventSummary(event: Event): String {
        val target = event.updatedTarget.character.bio.name

        return when (event) {
            is Event.WeaponAttackHit -> formatDamageHit(target, event.weaponDamageRoll.damage)
            is Event.WeaponAttackMissed -> formatAttackMiss(target)
            is Event.SpellAttackHit -> formatSpellHit(target, event.spellDamageRoll.damage)
            is Event.SpellAttackMissed -> formatSpellMiss(target)
            is Event.Healed -> formatHealing(target, event.healRoll.heal)
            is Event.EffectAdded -> formatEffectAdded(target, event.category)
            is Event.EffectRemoved -> formatEffectRemoved(target, event.category)
            is Event.ResourceBoosted -> formatResourceBoost(target, event.amount)
            is Event.ActionTaken -> formatActionTaken(target, event.resourceCost, event.newPositionNodeId)
            is Event.EffectsTicked -> formatEffectsTicked(target, event)
            is Event.InitiativeRolled -> formatInitiativeRolled(target, event.initiativeRoll.initiative)
        }
    }

    private fun formatDamageHit(target: String, damage: Int): String = "Hit $target for $damage damage"

    private fun formatAttackMiss(target: String): String = "Missed attack on $target"

    private fun formatSpellHit(target: String, damage: Int): String = "Spell hit $target for $damage damage"

    private fun formatSpellMiss(target: String): String = "Spell missed $target"

    private fun formatHealing(target: String, amount: Int): String = "Healed $target for $amount"

    private fun formatEffectAdded(target: String, category: Effect.Category): String = "Added ${getCategoryName(category)} to $target"

    private fun formatEffectRemoved(target: String, category: Effect.Category): String = "Removed ${getCategoryName(category)} from $target"

    private fun formatResourceBoost(target: String, amount: Int): String = "$target gained $amount resources"

    private fun formatActionTaken(target: String, cost: Int, nodeId: Int): String = "$target used $cost resources and moved to node $nodeId"

    private fun formatInitiativeRolled(target: String, initiative: Int): String = "$target rolled $initiative for initiative"

    private fun formatEffectsTicked(target: String, event: Event.EffectsTicked): String {
        val doT = event.damageOverTimeRolls.sumOf { it.roll.rolled }
        val hoT = event.healOverTimeRolls.sumOf { it.roll.rolled }

        return when {
            doT > 0 && hoT > 0 -> "$target took $doT DoT damage and healed $hoT from HoTs"
            doT > 0 -> "$target took $doT damage from DoTs"
            hoT > 0 -> "$target healed $hoT from HoTs"
            else -> "Effects ticked for $target"
        }
    }

    /**
     * Returns a detailed description of an effect
     */
    fun formatEffectDetails(effect: Effect): String {
        return when (effect) {
            // Non-timed effects
            is Effect.ActionForcingEffect.Dying -> "Dying"
            is Effect.ActionForcingEffect.Prone -> "Prone"

            // Timed effects - using a consistent format for all
            is Effect.ActionForcingEffect.Stun -> formatTimedEffect("Stunned", effect.roundsLeft)
            is Effect.ActionRestrictingEffect.Silenced -> formatTimedEffect("Silenced", effect.roundsLeft)
            is Effect.ActionRestrictingEffect.Disarmed -> formatTimedEffect("Disarmed", effect.roundsLeft)
            is Effect.MovementAlteringEffect.Slow -> formatTimedEffect("Slowed", effect.roundsLeft)
            is Effect.MovementAlteringEffect.Haste -> formatTimedEffect("Hastened", effect.roundsLeft)
            is Effect.MovementRestrictingEffect.Entangled -> formatTimedEffect("Entangled", effect.roundsLeft)
            is Effect.MovementRestrictingEffect.Held -> formatTimedEffect("Held", effect.roundsLeft)
            is Effect.DamageOverTimeEffect.Bleed -> formatTimedEffect("Bleeding", effect.roundsLeft)
            is Effect.DamageOverTimeEffect.Poison -> formatTimedEffect("Poisoned", effect.roundsLeft)
            is Effect.HealOverTimeEffect.Regeneration -> formatTimedEffect("Regeneration", effect.roundsLeft)

            // Fallback
            else -> "Unknown Effect"
        }
    }

    private fun formatTimedEffect(name: String, roundsLeft: Int): String {
        return "$name ($roundsLeft rounds)"
    }

    /**
     * Returns a summarized description of multiple effects, grouping by category and showing counts
     */
    fun formatEffectsList(effects: List<Effect>): String {
        if (effects.isEmpty()) return "None"

        // Group effects by category name
        val effectsByCategory = effects.groupBy { getCategoryName(it.category) }

        return effectsByCategory.entries.joinToString(", ") { (categoryName, categoryEffects) ->
            formatEffectCategory(categoryName, categoryEffects.size)
        }
    }

    private fun formatEffectCategory(categoryName: String, count: Int): String {
        return "$categoryName${if (count > 1) " ($count)" else ""}"
    }
}
