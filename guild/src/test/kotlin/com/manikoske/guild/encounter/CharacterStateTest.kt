package com.manikoske.guild.encounter

import com.manikoske.guild.character.CharacterState
import com.manikoske.guild.character.Status
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test


class CharacterStateTest {

    @Test
    fun `test takeDamage scenarios`() {
        // Arrange
        val character = mockk<com.manikoske.guild.character.Character> {
            every { maxHitPoints() } returns 100
        }

        val statusToRemove = Status(name = Status.Name.Entangled, removedOnDamageTaken = true)
        val statusOnHit = Status(name = Status.Name.Stunned)

        // Test Already Down
        val alreadyDownState = Fixture.characterState().copy(character = character, damageTaken = 100)
        val alreadyDownResult = alreadyDownState.takeDamage(50)
        assertThat(alreadyDownResult).isInstanceOf(CharacterState.Result.TakeDamageResult.AlreadyDown::class.java)

        // Test No Damage
        val noDamageState = Fixture.characterState().copy(character = character, damageTaken = 0)
        val noDamageResult = noDamageState.takeDamage(0)
        assertThat(noDamageResult).isInstanceOf(CharacterState.Result.TakeDamageResult.NoDamageTaken::class.java)

        // Test Downed
        val downedState =
            Fixture.characterState().copy(character = character, damageTaken = 50, statuses = listOf(statusToRemove))
        val downedResult = downedState.takeDamage(60, statusOnHit)
        assertThat(downedResult).isInstanceOf(CharacterState.Result.TakeDamageResult.Downed::class.java)
        (downedResult as CharacterState.Result.TakeDamageResult.Downed).also {
            assertThat(it.takenDamage).isEqualTo(50)
            assertThat(it.damagedOver).isEqualTo(10)
            assertThat(it.updatedTarget.statuses).doesNotContain(statusToRemove)
            assertThat(it.updatedTarget.statuses).contains(statusOnHit)
            assertThat(it.updatedTarget.statuses).contains(Status.StatusFactory.down())
            assertThat(it.updatedTarget.currentHitPoints()).isEqualTo(0)
        }

        // Test Status Changes
        val statusState =
            Fixture.characterState().copy(character = character, damageTaken = 0, statuses = listOf(statusToRemove))
        val statusResult = statusState.takeDamage(10, statusOnHit)
        assertThat(statusResult).isInstanceOf(CharacterState.Result.TakeDamageResult.StillStanding::class.java)
        (statusResult as CharacterState.Result.TakeDamageResult.StillStanding).also {
            assertThat(it.statusesRemovedOnDamage).contains(statusToRemove)
            assertThat(it.statusOnHit).isEqualTo(statusOnHit)
            assertThat(it.updatedTarget.statuses).doesNotContain(statusToRemove)
            assertThat(it.updatedTarget.statuses).contains(statusOnHit)
        }
    }

    @Test
    fun `test heal scenarios`() {
        // Arrange
        val character = mockk<com.manikoske.guild.character.Character> {
            every { maxHitPoints() } returns 100
        }

        // Test NoHeal
        val noHealState = Fixture.characterState().copy(character = character, damageTaken = 50)
        val noHealResult = noHealState.heal(0)
        assertThat(noHealResult).isInstanceOf(CharacterState.Result.ReceiveHealingResult.NoHeal::class.java)

        // Test AlreadyFull
        val alreadyFullState = Fixture.characterState().copy(character = character, damageTaken = 0)
        val alreadyFullResult = alreadyFullState.heal(20)
        assertThat(alreadyFullResult).isInstanceOf(CharacterState.Result.ReceiveHealingResult.AlreadyFull::class.java)

        // Test Partial Healing
        val partialHealState = Fixture.characterState().copy(character = character, damageTaken = 70)
        val partialHealResult = partialHealState.heal(40)
        assertThat(partialHealResult).isInstanceOf(CharacterState.Result.ReceiveHealingResult.Healed::class.java)
        (partialHealResult as CharacterState.Result.ReceiveHealingResult.Healed).also {
            assertThat(it.amountHealed).isEqualTo(40)
            assertThat(it.overHealed).isEqualTo(0)
            assertThat(it.updatedTarget.currentHitPoints()).isEqualTo(70)
        }

        // Test Over-healing
        val overHealState = Fixture.characterState().copy(character = character, damageTaken = 30)
        val overHealResult = overHealState.heal(50)
        assertThat(overHealResult).isInstanceOf(CharacterState.Result.ReceiveHealingResult.Healed::class.java)
        (overHealResult as CharacterState.Result.ReceiveHealingResult.Healed).also {
            assertThat(it.amountHealed).isEqualTo(30)
            assertThat(it.overHealed).isEqualTo(20)
            assertThat(it.updatedTarget.currentHitPoints()).isEqualTo(100)
        }
    }

}