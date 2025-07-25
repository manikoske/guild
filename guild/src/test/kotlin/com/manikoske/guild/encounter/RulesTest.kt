package com.manikoske.guild.encounter

import com.manikoske.guild.character.Character
import com.manikoske.guild.rules.Event
import com.manikoske.guild.rules.Roll
import com.manikoske.guild.rules.Rules
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class RulesTest {

    @Test
    fun testRollInitiative() {

        // define constants
        val initiativeRoll = Randomizer.randomBuilder().giveMeOne<Roll.InitiativeRoll>()

        // Mock for testing
        val character = mockk<Character>()
        every { character.initiativeRoll() } returns initiativeRoll

        val target = Randomizer.characterState().copy(character = character)

        // expected
        val expectedInitiativeRolled = Event.InitiativeRolled(
            target = target,
            updatedTarget = target,
            initiativeRoll = initiativeRoll
        )

        assertThat(Rules.rollInitiative(target)).isEqualTo(expectedInitiativeRolled)
    }

    @Test
    fun testStartAction() {
        // Constants for the test
        val actionName = "Charge"
        val newPositionNodeId = 5
        val resourcesSpent = 2

        val character = mockk<Character>()
        every { character.maxResources() } returns 10

        val target = Randomizer.characterState().copy(character = character)
        val updatedTarget = target.copy(positionNodeId = newPositionNodeId, resourcesSpent = resourcesSpent)

        // Expected result
        val expectedActionStarted = Event.ActionStarted(
            actionName = actionName,
            target = target,
            updatedTarget = updatedTarget,
            newPositionNodeId = newPositionNodeId,
            resourcesSpent = resourcesSpent
        )

        // Test the startAction method
        val result = Rules.startAction(
            target = target,
            actionName = actionName,
            newPositionNodeId = newPositionNodeId,
            resourcesSpent = resourcesSpent
        )

        // Verify the result
        assertThat(result).isEqualTo(expectedActionStarted)
    }


}