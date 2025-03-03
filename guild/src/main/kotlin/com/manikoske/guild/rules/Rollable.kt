package com.manikoske.guild.rules

sealed interface Rollable {

    fun roll() : Int

    data class Heal(
        private val roll : () -> Int,
    ) : Rollable {

        override fun roll(): Int {
            return roll.invoke()
        }
    }

    data class Damage(
        private val roll : () -> Int,
        // TODO Type
    ) : Rollable {

        override fun roll(): Int {
            return roll.invoke()
        }

        // TODO remove when fixturemonkey is fixed
        override fun toString(): String {
            return super.toString()
        }
    }

}