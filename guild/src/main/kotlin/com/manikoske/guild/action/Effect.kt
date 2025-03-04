package com.manikoske.guild.action

import com.manikoske.guild.rules.Rollable
import kotlin.math.max


sealed interface Effect {

    val category: String

    fun tick() : Effect? {
        return this
    }

    fun removeOnDamageTaken(): Boolean {
        return false
    }

    sealed class ActionForcingEffect : OnlyOneDeterminedBySeverity<ActionForcingEffect> {
        abstract fun forcedAction(): Action.ForcedAction

        override fun self(): ActionForcingEffect {
            return this
        }

        // TODO remove dummy when fixture monkey is fixed
        data class Prone(val dummy: Int) : ActionForcingEffect() {
            override fun severity(): Int {
                return 3
            }

            override fun forcedAction(): Action.ForcedAction {
                return Action.ForcedAction.StandUp
            }

            override val category: String
                get() = "Prone"
        }

        data class Stun(
            override val roundsLeft: Int
        ) : ActionForcingEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override fun severity(): Int {
                return 2
            }

            override fun forcedAction(): Action.ForcedAction {
                return Action.ForcedAction.NoAction
            }

            override val category: String
                get() = "Stun"

        }

        // TODO remove dummy when fixture monkey is fixed
        data class Dying(val dummy: Int) : ActionForcingEffect() {
            override fun severity(): Int {
                return 5
            }

            override fun forcedAction(): Action.ForcedAction {
                return Action.ForcedAction.FightForLife
            }

            override val category: String
                get() = "Dying"
        }
    }

    sealed class MovementAlteringEffect : ManyDeterminedByCategory<MovementAlteringEffect> {
        abstract fun alterActionMovement(movement: Movement): Movement


        override fun self(): MovementAlteringEffect {
            return this
        }

        data class Slow(
            override val roundsLeft: Int
        ) : MovementAlteringEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override val category: String
                get() = "Slow"



            override fun alterActionMovement(movement: Movement): Movement {
                return movement.let { it.copy(amount = max(it.amount - 1, 0)) }
            }
        }

        data class Haste(
            override val roundsLeft: Int
        ) : MovementAlteringEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override val category: String
                get() = "Haste"

            override fun alterActionMovement(movement: Movement): Movement {
                return movement.let { it.copy(amount = max(it.amount + 1, 0)) }
            }
        }

    }

    sealed class MovementRestrictingEffect : OnlyOneDeterminedBySeverity<MovementRestrictingEffect> {
        abstract fun restrictActionMovement(movement: Movement): Movement

        override fun self(): MovementRestrictingEffect {
            return this
        }

        data class Entangled(
            override val roundsLeft: Int
        ) : MovementRestrictingEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override val category: String
                get() = "Entangled"

            override fun restrictActionMovement(movement: Movement): Movement {
                return movement.let { if (it.type == Movement.Type.Normal) it.copy(amount = 0) else it }
            }

            override fun severity(): Int {
                return 1
            }

            override fun removeOnDamageTaken(): Boolean {
                return true
            }

        }

        data class Held(
            override val roundsLeft: Int
        ) : MovementRestrictingEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override val category: String
                get() = "Held"

            override fun restrictActionMovement(movement: Movement): Movement {
                return movement.copy(amount = 0)
            }

            override fun severity(): Int {
                return 2
            }
        }
    }

    sealed class ActionRestrictingEffect : ManyDeterminedByCategory<ActionRestrictingEffect> {
        abstract fun restrictedAction(action: Action): Boolean

        override fun self(): ActionRestrictingEffect {
            return this
        }

        data class Disarmed(
            override val roundsLeft: Int
        ) : ActionRestrictingEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override fun restrictedAction(action: Action): Boolean {
                return action.outcome is Outcome.AttackOutcome.WeaponAttack
            }

            override val category: String
                get() = "Disarmed"
        }

        data class Silenced(
            override val roundsLeft: Int
        ) : ActionRestrictingEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override fun restrictedAction(action: Action): Boolean {
                return action.outcome is Outcome.AttackOutcome.SpellAttack
            }

            override val category: String
                get() = "Silenced"
        }
    }

    sealed class DamageOverTimeEffect : ManyDeterminedByCategory<DamageOverTimeEffect>{
        abstract fun damage(): Rollable.Damage

        override fun self(): DamageOverTimeEffect {
            return this
        }

        data class Bleed(
            override val roundsLeft: Int,
            val damage: Rollable.Damage,
        ) : DamageOverTimeEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override val category: String
                get() = "Bleed"

            override fun damage(): Rollable.Damage {
                return damage
            }
        }

        data class Poison(
            override val roundsLeft: Int,
            val damage: Rollable.Damage,
        ) : DamageOverTimeEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override val category: String
                get() = "Poison"

            override fun damage(): Rollable.Damage {
                return damage
            }

        }
    }

    sealed class HealOverTimeEffect : ManyDeterminedByCategory<HealOverTimeEffect> {
        abstract fun heal(): Rollable.Heal

        override fun self(): HealOverTimeEffect {
            return this
        }

        data class Regeneration(
            override val roundsLeft: Int,
            val heal: Rollable.Heal,
        ) : HealOverTimeEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override fun heal(): Rollable.Heal {
                return heal
            }

            override val category: String
                get() = "Regeneration"
        }

    }

    sealed interface OnlyOneDeterminedBySeverity<T : OnlyOneDeterminedBySeverity<T>> : Effect {
        fun severity(): Int
        fun self(): T

        fun add(existingEffect: T?) : T {
            return if (existingEffect != null) {
                if (existingEffect.category == this.category) {
                    self()
                } else {
                    if (existingEffect.severity() > this.severity()) existingEffect else self()
                }
            } else {
                self()
            }
        }

        fun remove(existingEffect: T?) : T? {
            return if (existingEffect != null) {
                if (existingEffect.category== this.category) {
                    null
                } else {
                    existingEffect
                }
            } else {
                null
            }
        }
    }

    sealed interface ManyDeterminedByCategory<T : ManyDeterminedByCategory<T>> : Effect {
        fun self(): T

        fun add(existingEffects: List<T>): List<T> {
            return (existingEffects + self()).groupBy { it.category }.mapValues { it.value.last() }.values.toList()
        }

        fun remove(existingEffects: List<T>): List<T> {
            return existingEffects.filter { it.category != this.category }
        }
    }

    sealed interface TimedEffect : Effect {

        val roundsLeft: Int

        fun nextRoundEffect(roundsLeft: Int): Effect

        override fun tick(): Effect? {
            return (roundsLeft - 1).let { if (it > 0)  nextRoundEffect(it) else null }
        }

    }

}