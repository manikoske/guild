package com.manikoske.guild.character

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.rules.Dice
import kotlin.math.max

sealed interface Effect {

    fun tick() : Effect? {
        return this
    }

    fun removeOnDamageTaken(): Boolean {
        return false
    }

    fun removeOnMovement(): Boolean {
        return false
    }

    sealed class TargetabilityRestrictingEffect : OnlyOneDeterminedBySeverity<TargetabilityRestrictingEffect> {

        override fun self(): TargetabilityRestrictingEffect {
            return this
        }

        override fun removeOnMovement(): Boolean {
            return true
        }


        data class Hidden(val dummy: Int) : TargetabilityRestrictingEffect() {
            override fun severity(): Int {
                return 1
            }
        }

        data class Invisible(override val roundsLeft: Int) : TargetabilityRestrictingEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

            override fun severity(): Int {
                return 2
            }
        }

        data class Ethereal(val dummy: Int) : TargetabilityRestrictingEffect() {
            override fun severity(): Int {
                return 3
            }
        }

    }

    sealed class ActionForcingEffect : OnlyOneDeterminedBySeverity<ActionForcingEffect> {
        abstract fun forcedAction(): Action

        override fun self(): ActionForcingEffect {
            return this
        }

        // TODO remove dummy when fixture monkey is fixed
        data class Prone(val dummy: Int) : ActionForcingEffect() {
            override fun severity(): Int {
                return 3
            }

            override fun forcedAction(): Action {
                return Action.Actions.standUp
            }
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

        }

        // TODO remove dummy when fixture monkey is fixed
        data class Downed(val dummy: Int) : ActionForcingEffect() {
            override fun severity(): Int {
                return 5
            }

            override fun forcedAction(): Action {
                return Action.Actions.fightForLife
            }

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
                return action is Action.TargetedAction.AttackAction.WeaponAttack
            }
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
        }
    }

    sealed class DamageOverTimeEffect : ManyDeterminedByCategory<DamageOverTimeEffect>{

        abstract val damageDice: Dice

        override fun self(): DamageOverTimeEffect {
            return this
        }

        data class Bleed(
            override val roundsLeft: Int,
            override val damageDice: Dice,
        ) : DamageOverTimeEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

        }

        data class Poison(
            override val roundsLeft: Int,
            override val damageDice: Dice,
        ) : DamageOverTimeEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

        }
    }

    sealed class HealOverTimeEffect : ManyDeterminedByCategory<HealOverTimeEffect> {

        abstract val healDice: Dice

        override fun self(): HealOverTimeEffect {
            return this
        }


        data class Regeneration(
            override val roundsLeft: Int,
            override val healDice: Dice,
        ) : HealOverTimeEffect(), TimedEffect {

            override fun nextRoundEffect(roundsLeft: Int): Effect {
                return copy(roundsLeft = roundsLeft)
            }

        }

    }

    

    sealed interface OnlyOneDeterminedBySeverity<T : OnlyOneDeterminedBySeverity<T>> : Effect {
        fun severity(): Int
        fun self(): T

        fun add(existingEffect: T?) : T {
            return if (existingEffect != null) {
                if (existingEffect.javaClass.name == this.javaClass.name) {
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
                if (existingEffect.javaClass.name == this.javaClass.name) {
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
            return (existingEffects + self()).groupBy { it.javaClass.name }.mapValues { it.value.last() }.values.toList()
        }

        fun remove(existingEffects: List<T>): List<T> {
            return existingEffects.filter { it.javaClass.name != this.javaClass.name }
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