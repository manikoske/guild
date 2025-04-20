package com.manikoske.guild.rules

sealed interface Rollable {

    fun roll() : Int

    data class Heal(
        private val dice : Die.Dice,
    ) : Rollable {

        override fun roll(): Int {
            return dice.roll()
        }
    }

    data class Damage(
        private val dice : Die.Dice,
        // TODO Type
    ) : Rollable {

        override fun roll(): Int {
            return dice.roll()
        }

        // TODO remove when fixturemonkey is fixed
        override fun toString(): String {
            return super.toString()
        }
    }

}