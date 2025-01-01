package com.manikoske.guild.ability


sealed interface Effect {

    object Effects {

        val opportunityAttackImmunity = OpportunityAttackImmunity(
            name = "Immunity to Opportunity Attack",
            roundsLeft = 1,
        )

        val doubleMovement = DoubleMovement(
            name = "Double Movement",
            roundsLeft = 1,
        )

        val stunned = Stunned(
            name = "Stunned",
            roundsLeft = 1,
        )

        val entangled = Entangled(
            name = "Entangled",
            roundsLeft = 1,
        )
    }

    val name: String
    val roundsLeft: Int

    data class OpportunityAttackImmunity(
        override val name: String,
        override val roundsLeft: Int
    ) : Effect

    data class DoubleMovement(
        override val name: String,
        override val roundsLeft: Int
    ) : Effect

    data class Stunned(
        override val name: String,
        override val roundsLeft: Int
    ) : Effect

    data class Entangled(
        override val name: String,
        override val roundsLeft: Int
    ) : Effect

    data class DamageOverTime(
        override val name: String,
        override val roundsLeft: Int,
        val damageRoll: () -> Int,
    ) : Effect


}