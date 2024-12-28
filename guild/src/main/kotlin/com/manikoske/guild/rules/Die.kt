package com.manikoske.guild.rules

enum class Die(val sides: Int) {

    d2(2),
    d4(4),
    d6(6),
    d8(8),
    d10(10),
    d12(12),
    d20(20);

    fun roll(times: Int, modifier: Int = 0): Int {
        return (1..times).sumOf { (1..sides).random() } + modifier
    }

}