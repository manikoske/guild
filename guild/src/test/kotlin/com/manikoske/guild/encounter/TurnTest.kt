package com.manikoske.guild.encounter

import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test

class TurnTest {

    @Test
    fun testSimulate() {

        val sarevok = Randomizer.characterState("Sarevok")
        every { chockCharacter.initiativeRoll() } returns 10
        every { chockCharacter.id } returns 1

        val turn = Turn(takerId = 1, characterStates = listOf())
    }

}