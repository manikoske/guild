package com.manikoske.guild.encounter

import com.manikoske.guild.character.CharacterState
import com.manikoske.guild.character.Status
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test


class CharacterStateTest {

    @Test
    fun `test already down`() {
        // Arrange
        val character = mockk<com.manikoske.guild.character.Character> {
            every { maxHitPoints() } returns 100
        }

        val state = Fixture.characterState().copy(character = character, damageTaken = 100)

        // Act
        val result = state.takeDamage(50)

        // Assert
        assertThat(result).isInstanceOf(CharacterState.Result.TakeDamageResult.AlreadyDown::class.java)
    }

    @Test
    fun `test no damage taken`() {
        // Arrange
        val character = mockk<com.manikoske.guild.character.Character> {
            every { maxHitPoints() } returns 100
        }
        val state = Fixture.characterState().copy(character = character, damageTaken = 0)

        // Act
        val result = state.takeDamage(0)

        // Assert
        assertThat(result).isInstanceOf(CharacterState.Result.TakeDamageResult.NoDamageTaken::class.java)
    }


    @Test
    fun `test take damage and downed`() {
        // Arrange
        val character = mockk<com.manikoske.guild.character.Character> {
            every { maxHitPoints() } returns 100
        }

        val statusToRemove = Status(name = Status.Name.Entangled, removedOnDamageTaken = true)
        val state = Fixture.characterState().copy(character = character, damageTaken = 50, statuses = listOf(statusToRemove))
        val statusOnHit = Status(name = Status.Name.Stunned)


        // Act
        val result = state.takeDamage(60, statusOnHit)

        // Assert
        assertThat(result).isInstanceOf(CharacterState.Result.TakeDamageResult.Downed::class.java)
        val downedResult = result as CharacterState.Result.TakeDamageResult.Downed
        assertThat(downedResult.takenDamage).isEqualTo(50)
        assertThat(downedResult.damagedOver).isEqualTo(10)
        assertThat(downedResult.updatedTarget.statuses).doesNotContain(statusToRemove)
        assertThat(downedResult.updatedTarget.statuses).contains(statusOnHit)
        assertThat(downedResult.updatedTarget.statuses).contains(Status.StatusFactory.down())
        assertThat(downedResult.updatedTarget.currentHitPoints()).isEqualTo(0)
    }

    @Test
    fun `test statuses removed and added on damage`() {
        // Arrange
        val character = mockk<com.manikoske.guild.character.Character> {
            every { maxHitPoints() } returns 100
        }
        val statusToRemove = Status(name = Status.Name.Entangled, removedOnDamageTaken = true)
        val state = Fixture.characterState().copy(character = character, damageTaken = 0, statuses = listOf(statusToRemove))

        val statusOnHit = Status(name = Status.Name.Stunned)

        // Act
        val result = state.takeDamage(10, statusOnHit)

        // Assert
        assertThat(result).isInstanceOf(CharacterState.Result.TakeDamageResult.StillStanding::class.java)
        val stillStandingResult = result as CharacterState.Result.TakeDamageResult.StillStanding
        assertThat(stillStandingResult.statusesRemovedOnDamage).contains(statusToRemove)
        assertThat(stillStandingResult.statusOnHit).isEqualTo(statusOnHit)
        assertThat(stillStandingResult.updatedTarget.statuses).doesNotContain(statusToRemove)
        assertThat(stillStandingResult.updatedTarget.statuses).contains(statusOnHit)
    }

}