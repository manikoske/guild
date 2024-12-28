package com.manikoske.guild.rules


sealed interface Effect {

    object Effects {

        val disengaged = Disengaged(
            name = "Disengaged",
            roundsLeft = 1,
        )

        val doubleMovement = DoubleMovement(
            name = "Double Movement",
            roundsLeft = 1,
        )


        private val effects = listOf(
            disengaged,
            doubleMovement
        )

    }

    val name: String
    val roundsLeft: Int

    data class Disengaged(
        override val name: String,
        override val roundsLeft: Int
    ) : Effect

    data class DoubleMovement(
        override val name: String,
        override val roundsLeft: Int
    ) : Effect


}