package com.manikoske.guild.rules

enum class Die(val sides: Int) {

    d2(2),
    d4(4),
    d6(6),
    d8(8),
    d10(10),
    d12(12),
    d20(20);

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

        fun roll(): Int {
            return dice.sumOf { (1..it.sides).random() } + modifier
        }
    }

    data class Roll(
        val dice: Dice,
    ) {
        val rolled: Int = dice.roll()
    }
}