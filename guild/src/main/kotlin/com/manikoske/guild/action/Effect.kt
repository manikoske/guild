package com.manikoske.guild.action

import com.manikoske.guild.rules.Die
import kotlin.math.max


sealed interface Effect {

    val category: Category

    sealed interface Category

    fun tick() : Effect? {
        return this
    }

    fun removeOnDamageTaken(): Boolean {
        return false
    }

    sealed class ActionForcingEffect : OnlyOneDeterminedBySeverity<ActionForcingEffect> {
        abstract fun forcedAction(): Action

        override fun self(): ActionForcingEffect {
            return this
        }

        enum class Category : Effect.Category {
            Prone, Stun, Dying
        }

        // TODO remove dummy when fixture monkey is fixed
        data class Prone(val dummy: Int) : ActionForcingEffect() {
            override fun severity(): Int {
                return 3
            }

            override fun forcedAction(): Action {
                return Action.Actions.standUp
            }

            override val category: Category
                get() = Category.Prone
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

            override fun forcedAction(): Action {
                return Action.Actions.noAction
            }

            override val category: Category
                get() = Category.Stun

        }

        // TODO remove dummy when fixture monkey is fixed
        data class Dying(val dummy: Int) : ActionForcingEffect() {
            override fun severity(): Int {
                return 5
            }

            override fun forcedAction(): Action {
                return Action.Actions.fightForLife
            }

            override val category: Category
                get() = Category.Dying
        }
    }

    sealed class MovementAlteringEffect : ManyDeterminedByCategory<MovementAlteringEffect> {
        abstract fun alterActionMovement(movement: Movement): Movement


        override fun self(): MovementAlteringEffect {
            return this
        }

        enum class Category : Effect.Category {
            Slow, Haste
        }

        data class Slow(
            override val roundsLeft: Int
        ) : MovementAlteringEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override val category: Category
                get() = Category.Slow

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

            override val category: Category
                get() = Category.Haste

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

        enum class Category : Effect.Category {
            Entangled, Held
        }

        data class Entangled(
            override val roundsLeft: Int
        ) : MovementRestrictingEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override val category: Category
                get() = Category.Entangled

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

            override val category: Category
                get() = Category.Held

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

        enum class Category : Effect.Category {
            Disarmed, Silenced
        }

        data class Disarmed(
            override val roundsLeft: Int
        ) : ActionRestrictingEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override fun restrictedAction(action: Action): Boolean {
                return action is Action.TargetedAction.AttackAction.WeaponAttack
            }

            override val category: Category
                get() = Category.Disarmed
        }

        data class Silenced(
            override val roundsLeft: Int
        ) : ActionRestrictingEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override fun restrictedAction(action: Action): Boolean {
                return action is Action.TargetedAction.AttackAction.WeaponAttack
            }

            override val category: Category
                get() = Category.Silenced
        }
    }

    sealed class DamageOverTimeEffect : ManyDeterminedByCategory<DamageOverTimeEffect>{

        abstract val damageDice: Die.Dice

        override fun self(): DamageOverTimeEffect {
            return this
        }

        enum class Category : Effect.Category {
            Bleed, Poison
        }

        data class Bleed(
            override val roundsLeft: Int,
            override val damageDice: Die.Dice,
        ) : DamageOverTimeEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override val category: Category
                get() = Category.Bleed

        }

        data class Poison(
            override val roundsLeft: Int,
            override val damageDice: Die.Dice,
        ) : DamageOverTimeEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override val category: Category
                get() = Category.Poison

        }
    }

    sealed class HealOverTimeEffect : ManyDeterminedByCategory<HealOverTimeEffect> {

        abstract val healDice: Die.Dice

        override fun self(): HealOverTimeEffect {
            return this
        }

        enum class Category : Effect.Category {
            Regeneration
        }

        data class Regeneration(
            override val roundsLeft: Int,
            override val healDice: Die.Dice,
        ) : HealOverTimeEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override val category: Category
                get() = Category.Regeneration
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