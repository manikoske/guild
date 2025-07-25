package com.manikoske.guild.rules

data class Dice(
    val dice: List<Die>,
    val modifier: Int
) {

    companion object {
        fun of(die: Die) : Dice {
            return Dice(dice = listOf(die), modifier = 0)
        }

        fun of(times: Int, die: Die) : Dice {
            return Dice(dice = (1..times).map { die }, modifier = 0)
        }

        fun combine(first: Dice, second: Dice) : Dice {
            return Dice(dice = first.dice + second.dice, modifier = first.modifier + second.modifier)
        }
    }

    fun roll(method: RollMethod) : Int {
        return when (method) {
            RollMethod.Normal -> dice.sumOf { (1..it.sides).random() } + modifier
            RollMethod.Median -> dice.sumOf { it.sides / 2 } + modifier
        }
    }

    enum class RollMethod {
        Normal,
        Median
    }


}