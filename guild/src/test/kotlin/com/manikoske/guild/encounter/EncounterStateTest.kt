package com.manikoske.guild.encounter

import org.junit.jupiter.api.Test

class EncounterStateTest {


    @Test
    fun testRollInitiatives() {
        val minsc = Randomizer.characterState("Minsc").copy(positionNodeId = 2, allegiance = CharacterState.Allegiance.Attacker)


    }

    @Test
    fun hasNoWinner() {

    }

}