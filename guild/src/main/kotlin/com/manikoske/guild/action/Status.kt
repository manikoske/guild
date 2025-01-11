package com.manikoske.guild.action


sealed interface Status {

    val name: String
    val roundsLeft: Int

    data class Stunned(
        override val roundsLeft: Int
    ) : Status {
        override val name: String
            get() = "Stunned"
    }

    data class Entangled(
        override val roundsLeft: Int
    ) : Status {
        override val name: String
            get() = "Entangled"
    }

    data class DamageOverTime(
        override val name: String,
        override val roundsLeft: Int,
        val damageRoll: () -> Int,
    ) : Status


}