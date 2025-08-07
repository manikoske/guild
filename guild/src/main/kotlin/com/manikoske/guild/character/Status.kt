package com.manikoske.guild.character

data class Status(
    val name: String,
    val category: Category,
    val duration: Duration = Duration.Permanent,
    val removedOnDamageTaken : Boolean = false,
    val removedOnMovement : Boolean = false,
    val removedOnDamageDone : Boolean = false,
    val actionForcingEffect: Effect.ActionForcingEffect? = null,
    val targetabilityRestrictingEffect: Effect.TargetabilityRestrictingEffect? = null,
    val movementRestrictingEffect: Effect.MovementRestrictingEffect? = null,
    val movementAlteringEffects: Effect.MovementAlteringEffect? = null,
    val actionRestrictingEffects: Effect.ActionRestrictingEffect? = null,
    val damageOverTimeEffects: Effect.DamageOverTimeEffect? = null,
    val healOverTimeEffects: Effect.HealOverTimeEffect? = null
) {

    object StatusFactory {

        fun prone() = Status(
            name = "Prone",
            category = Category.Debilitating,
            actionForcingEffect = Effect.ActionForcingEffect(
                // zamysliet sa nad povinnostou sverity
            ),
        )

    }

    fun tick() : Status {
        return this.copy(
            duration = when(duration) {
                is Duration.RoundLimited -> Duration.RoundLimited(duration.roundsLeft - 1)
                is Duration.Permanent -> Duration.Permanent
            }
        )
    }

    enum class Category {
        Debilitating
    }

    sealed interface Duration {
        data class RoundLimited(val roundsLeft: Int): Duration
        data object Permanent: Duration
    }
}