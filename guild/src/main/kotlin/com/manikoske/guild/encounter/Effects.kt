package com.manikoske.guild.encounter

import com.manikoske.guild.action.Effect

data class Effects(
    val actionForcingEffect: Effect.ActionForcingEffect?,
    val movementRestrictingEffect: Effect.MovementRestrictingEffect?,
    val movementAlteringEffects: List<Effect.MovementAlteringEffect>,
    val actionRestrictingEffects: List<Effect.ActionRestrictingEffect>,
    val damageOverTimeEffects: List<Effect.DamageOverTimeEffect>,
    val healOverTimeEffects: List<Effect.HealOverTimeEffect>
) {

    fun add(effect: Effect): Effects {
        return when (effect) {
            is Effect.ActionForcingEffect -> this.copy(actionForcingEffect = effect.add(actionForcingEffect))
            is Effect.MovementRestrictingEffect -> this.copy(
                movementRestrictingEffect = effect.add(
                    movementRestrictingEffect
                )
            )

            is Effect.ActionRestrictingEffect -> this.copy(
                actionRestrictingEffects = effect.add(
                    actionRestrictingEffects
                )
            )

            is Effect.DamageOverTimeEffect -> this.copy(damageOverTimeEffects = effect.add(damageOverTimeEffects))
            is Effect.MovementAlteringEffect -> this.copy(movementAlteringEffects = effect.add(movementAlteringEffects))
            is Effect.HealOverTimeEffect -> this.copy(healOverTimeEffects = effect.add(healOverTimeEffects))
        }
    }

    fun remove(effect: Effect): Effects {
        return when (effect) {
            is Effect.ActionForcingEffect -> this.copy(actionForcingEffect = effect.remove(actionForcingEffect))
            is Effect.MovementRestrictingEffect -> this.copy(
                movementRestrictingEffect = effect.remove(
                    movementRestrictingEffect
                )
            )

            is Effect.ActionRestrictingEffect -> this.copy(
                actionRestrictingEffects = effect.remove(
                    actionRestrictingEffects
                )
            )

            is Effect.DamageOverTimeEffect -> this.copy(damageOverTimeEffects = effect.remove(damageOverTimeEffects))
            is Effect.MovementAlteringEffect -> this.copy(
                movementAlteringEffects = effect.remove(
                    movementAlteringEffects
                )
            )

            is Effect.HealOverTimeEffect -> this.copy(healOverTimeEffects = effect.remove(healOverTimeEffects))
        }
    }

    fun removeOnDamage(): Effects {
        return all()
            .filter { it.removeOnDamageTaken() }
            .fold(this) { effects: Effects, effect: Effect -> effects.remove(effect) }
    }

    fun tick(): Effects {
        return all()
            .fold(this) { effects: Effects, effect: Effect ->
                effect.tick().let {
                    if (it != null) effects.add(it) else effects.remove(effect)
                }
            }
    }

    fun all(): List<Effect> {
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
}