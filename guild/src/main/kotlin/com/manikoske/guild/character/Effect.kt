package com.manikoske.guild.character

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Target
import com.manikoske.guild.rules.Dice

sealed interface Effect {


    data class TargetabilityAlteringEffect(val targetableBy: Set<Target.Type>)

    sealed interface ActionAvailabilityAlteringEffect {

        object NoActionForcingEffect : ActionAvailabilityAlteringEffect

        data class ActionsForcingEffect(val forcedActions: List<Action>) : ActionAvailabilityAlteringEffect

        data class ActionRestrictingEffect(val predicate: (Action) -> Boolean) : ActionAvailabilityAlteringEffect
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