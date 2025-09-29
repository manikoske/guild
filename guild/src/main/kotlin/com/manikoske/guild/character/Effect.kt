package com.manikoske.guild.character

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Target
import com.manikoske.guild.rules.Dice

sealed interface Effect {

    // TODO add targeting preference effect - taunt(id) - restrict by id, random target - disoriented, nearest target - blind (lower speed)

    // TODO add effect for changing allegiance - charmed(removed on damage taken), domination

    data class TargetabilityAlteringEffect(val targetableBy: Set<Target.Type>)

    sealed interface ActionAvailabilityAlteringEffect {

        object NoActionForcingEffect : ActionAvailabilityAlteringEffect

        data class ActionsForcingEffect(val forcedActions: List<Action>) : ActionAvailabilityAlteringEffect

        data class ActionRestrictingEffect(val predicate: (Action) -> Boolean) : ActionAvailabilityAlteringEffect

        // TODO add effect for random picking action - confusion
    }

    sealed interface ActionMovementAlteringEffect {

        data class ActionMovementAmountAlteringEffect(val alteration: (Movement) -> Movement) : ActionMovementAlteringEffect

        data class ActionMovementRestrictingEffect(val restriction: (Movement) -> Movement) : ActionMovementAlteringEffect

    }

    sealed interface HpAffectingOverTimeEffect {

        data class DamageOverTimeEffect(val damageDice: Dice) : HpAffectingOverTimeEffect

        data class HealingOverTimeEffect(val healDice: Dice) : HpAffectingOverTimeEffect

    }

}