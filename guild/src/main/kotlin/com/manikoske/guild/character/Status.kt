package com.manikoske.guild.character

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.rules.Dice
import kotlin.math.max

data class Status(
    val name: Name,
    val duration: Duration = Duration.Permanent,
    val removedOnDamageTaken : Boolean = false,
    val removedOnMovement : Boolean = false,
    val removedOnDamageDone : Boolean = false, // todo implement
    val targetabilityAlteringEffect: Effect.TargetabilityAlteringEffect? = null,
    val actionAvailabilityAlteringEffect: Effect.ActionAvailabilityAlteringEffect? = null,
    val actionMovementAlteringEffect: Effect.ActionMovementAlteringEffect? = null,
    val hpAffectingOverTimeEffect: Effect.HpAffectingOverTimeEffect? = null,
) {

    object StatusFactory {

        fun prone() = Status(
            name = Name.Prone,
            actionAvailabilityAlteringEffect = Effect.ActionAvailabilityAlteringEffect.ActionsForcingEffect(listOf(Action.Actions.crawl, Action.Actions.standUp)),
            targetabilityAlteringEffect = Effect.TargetabilityAlteringEffect.TargetabilityRestrictingEffect({ true }), // TODO define predicate
        )

        fun stun(roundsLeft: Int) = Status(
            name = Name.Stunned,
            duration = Duration.RoundLimited(roundsLeft),
            actionAvailabilityAlteringEffect = Effect.ActionAvailabilityAlteringEffect.NoActionForcingEffect,
        )

        fun down() = Status(
            name = Name.Downed,
            actionAvailabilityAlteringEffect = Effect.ActionAvailabilityAlteringEffect.NoActionForcingEffect,
            targetabilityAlteringEffect = Effect.TargetabilityAlteringEffect.TargetabilityPreventingEffect
        )

        fun silence(roundsLeft: Int) = Status(
            name = Name.Silenced,
            duration = Duration.RoundLimited(roundsLeft),
            actionAvailabilityAlteringEffect = Effect.ActionAvailabilityAlteringEffect.ActionRestrictingEffect {
                it !is Action.TargetedAction.AttackAction.SpellAttack
            },
        )

        fun disarm(roundsLeft: Int) = Status(
            name = Name.Disarmed,
            duration = Duration.RoundLimited(roundsLeft),
            actionAvailabilityAlteringEffect = Effect.ActionAvailabilityAlteringEffect.ActionRestrictingEffect {
                it !is Action.TargetedAction.AttackAction.WeaponAttack
            },
        )

        fun slow(roundsLeft: Int) = Status(
            name = Name.Slow,
            duration = Duration.RoundLimited(roundsLeft),
            actionMovementAlteringEffect = Effect.ActionMovementAlteringEffect.ActionMovementAmountAlteringEffect {
                it.let { it.copy(amount = max(it.amount - 1, 0)) }
            }
        )

        fun haste(roundsLeft: Int) = Status(
            name = Name.Haste,
            duration = Duration.RoundLimited(roundsLeft),
            actionMovementAlteringEffect = Effect.ActionMovementAlteringEffect.ActionMovementAmountAlteringEffect {
                it.let { it.copy(amount = it.amount + 1) }
            }
        )

        fun held(roundsLeft: Int) = Status(
            name = Name.Held,
            duration = Duration.RoundLimited(roundsLeft),
            actionMovementAlteringEffect = Effect.ActionMovementAlteringEffect.ActionMovementRestrictingEffect {
                it.copy(amount = 0)
            }
        )

        fun entangle(roundsLeft: Int) = Status(
            name = Name.Entangled,
            removedOnDamageTaken = true,
            duration = Duration.RoundLimited(roundsLeft),
            actionMovementAlteringEffect = Effect.ActionMovementAlteringEffect.ActionMovementRestrictingEffect {
                it.let { if (it.type == Movement.Type.Normal) it.copy(amount = 0) else it }
            }
        )

        fun regeneration(roundsLeft: Int, healDice: Dice) = Status(
            name = Name.Regenerating,
            duration = Duration.RoundLimited(roundsLeft),
            hpAffectingOverTimeEffect = Effect.HpAffectingOverTimeEffect.HealingOverTimeEffect(healDice)
        )

        fun poison(roundsLeft: Int, damageDice: Dice) = Status(
            name = Name.Poisoned,
            duration = Duration.RoundLimited(roundsLeft),
            hpAffectingOverTimeEffect = Effect.HpAffectingOverTimeEffect.DamageOverTimeEffect(damageDice)
        )

        fun hidden() = Status(
            name = Name.Hidden,
            removedOnMovement = true,
            targetabilityAlteringEffect = Effect.TargetabilityAlteringEffect.TargetabilityRestrictingEffect({ true }), // TODO define predicate
        )

    }

    fun tick() : TickResult {
        return when (duration) {
            Duration.Permanent -> TickResult.NoChange
            is Duration.RoundLimited -> if (duration.roundsLeft == 1) {
                TickResult.Remove(this)
            } else {
                TickResult.Update(this.copy(duration = Duration.RoundLimited(duration.roundsLeft - 1)))
            }
        }
    }

    sealed interface TickResult {
        data object NoChange : TickResult
        data class Remove(val removedStatus: Status) : TickResult
        data class Update(val updatedStatus: Status) : TickResult
    }

    enum class Name {
        Prone,
        Stunned,
        Downed,
        Poisoned,
        Regenerating,
        Held,
        Entangled,
        Silenced,
        Disarmed,
        Slow,
        Haste,
        Hidden,
        Invisible,
        Sanctuary

    }

    sealed interface Duration {

        data class RoundLimited(val roundsLeft: Int): Duration

        data object Permanent: Duration
    }
}