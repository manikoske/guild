package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
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
    fun `test tickStatuses scenarios`() {
        // Arrange
        val statusToUpdate = Status.StatusFactory.haste(2)
        val statusToRemove = Status.StatusFactory.stun(1)
        val statusToRemain = Status.StatusFactory.down()

        val character = mockk<com.manikoske.guild.character.Character> {
            every { maxHitPoints() } returns 100
        }
        val initialCharacterState = Fixture.characterState().copy(
            character = character,
            statuses = listOf(statusToUpdate, statusToRemove, statusToRemain)
        )

        // Act
        val result = initialCharacterState.tickStatuses()
        val updatedStatus = statusToUpdate.copy(duration = Status.Duration.RoundLimited(1))

        // Assert
        assertThat(result.removedStatuses).containsExactly(statusToRemove)
        assertThat(result.updatedStatuses).containsExactly(updatedStatus)
        assertThat(result.updatedTarget.statuses).containsExactly(updatedStatus, statusToRemain)
    }

    @Test
    fun `test spendResources scenarios`() {
        // Arrange
        val character = mockk<com.manikoske.guild.character.Character> {
            every { maxResources() } returns 50
        }

        // Test NoResourcesSpent
        val noResourcesState = Fixture.characterState().copy(character = character, resourcesSpent = 20)
        val noResourcesResult = noResourcesState.spendResources(0)
        assertThat(noResourcesResult).isInstanceOf(CharacterState.Result.SpendResourcesResult.NoResourcesSpent::class.java)
        (noResourcesResult as CharacterState.Result.SpendResourcesResult.NoResourcesSpent).also {
            assertThat(it.resourcesRemaining).isEqualTo(30)
        }

        // Test ResourcesSpent
        val spendResourcesState = Fixture.characterState().copy(character = character, resourcesSpent = 20)
        val spendResourcesResult = spendResourcesState.spendResources(10)
        assertThat(spendResourcesResult).isInstanceOf(CharacterState.Result.SpendResourcesResult.ResourcesSpent::class.java)
        (spendResourcesResult as CharacterState.Result.SpendResourcesResult.ResourcesSpent).also {
            assertThat(it.spentAmount).isEqualTo(10)
            assertThat(it.resourcesRemaining).isEqualTo(20)
            assertThat(it.updatedTarget.currentResources()).isEqualTo(20)
        }

        // Test InsufficientResources
        val insufficientResourcesState = Fixture.characterState().copy(character = character, resourcesSpent = 40)
        val insufficientResourcesResult = insufficientResourcesState.spendResources(20)
        assertThat(insufficientResourcesResult).isInstanceOf(CharacterState.Result.SpendResourcesResult.InsufficientResources::class.java)
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

    @Test
    fun `test boostResources scenarios`() {
        // Arrange
        val character = mockk<com.manikoske.guild.character.Character> {
            every { maxResources() } returns 50
        }

        // Test NoResourceBoost
        val noResourceBoostState = Fixture.characterState().copy(character = character, resourcesSpent = 30)
        val noResourceBoostResult = noResourceBoostState.boostResources(0)
        assertThat(noResourceBoostResult).isInstanceOf(CharacterState.Result.BoostResourcesResult.NoResourceBoost::class.java)

        // Test AlreadyFull
        val alreadyFullState = Fixture.characterState().copy(character = character, resourcesSpent = 0)
        val alreadyFullResult = alreadyFullState.boostResources(10)
        assertThat(alreadyFullResult).isInstanceOf(CharacterState.Result.BoostResourcesResult.AlreadyFull::class.java)

        // Test Partial Resource Boost
        val partialBoostState = Fixture.characterState().copy(character = character, resourcesSpent = 30)
        val partialBoostResult = partialBoostState.boostResources(20)
        assertThat(partialBoostResult).isInstanceOf(CharacterState.Result.BoostResourcesResult.ResourcesBoosted::class.java)
        (partialBoostResult as CharacterState.Result.BoostResourcesResult.ResourcesBoosted).also {
            assertThat(it.amountBoosted).isEqualTo(20)
            assertThat(it.overBoosted).isEqualTo(0)
            assertThat(it.updatedTarget.currentResources()).isEqualTo(40)
        }

        // Test Over-boosted Resources
        val overBoostState = Fixture.characterState().copy(character = character, resourcesSpent = 20)
        val overBoostResult = overBoostState.boostResources(50)
        assertThat(overBoostResult).isInstanceOf(CharacterState.Result.BoostResourcesResult.ResourcesBoosted::class.java)
        (overBoostResult as CharacterState.Result.BoostResourcesResult.ResourcesBoosted).also {
            assertThat(it.amountBoosted).isEqualTo(20)
            assertThat(it.overBoosted).isEqualTo(30)
            assertThat(it.updatedTarget.currentResources()).isEqualTo(50)
        }
    }

    @Test
    fun `test moveTo scenarios`() {
        // Arrange
        val character = mockk<com.manikoske.guild.character.Character>()

        val state = Fixture.characterState().copy(character = character, positionNodeId = 1, statuses = emptyList())
        val statusToRemove = Status(name = Status.Name.Entangled, removedOnMovement = true)
        val stateWithRemovableStatus = Fixture.characterState().copy(
            character = character,
            positionNodeId = 1,
            statuses = listOf(statusToRemove)
        )

        // Test NoMovement
        val noMovementResult = state.moveTo(1)
        assertThat(noMovementResult).isInstanceOf(CharacterState.Result.MovementResult.NoMovement::class.java)

        // Test Movement without statuses
        val movementResult = state.moveTo(2)
        assertThat(movementResult).isInstanceOf(CharacterState.Result.MovementResult.Movement::class.java)
        (movementResult as CharacterState.Result.MovementResult.Movement).also {
            assertThat(it.newPositionNodeIde).isEqualTo(2)
            assertThat(it.statusesRemovedByMovement).isEmpty()
        }

        // Test Movement with statuses removed
        val movementWithStatusResult = stateWithRemovableStatus.moveTo(2)
        assertThat(movementWithStatusResult).isInstanceOf(CharacterState.Result.MovementResult.Movement::class.java)
        (movementWithStatusResult as CharacterState.Result.MovementResult.Movement).also {
            assertThat(it.newPositionNodeIde).isEqualTo(2)
            assertThat(it.statusesRemovedByMovement).containsExactly(statusToRemove)
            assertThat(it.updatedTarget.statuses).doesNotContain(statusToRemove)
        }
    }

    @Test
    fun `test targetableBy scenarios`() {
        // Arrange
        val character = mockk<com.manikoske.guild.character.Character>()

        val noEffectStatus = mockk<Status> {
            every { targetabilityAlteringEffect } returns null
        }

        val restrictSingleAllyStatus = mockk<Status> {
            every { targetabilityAlteringEffect } returns mockk {
                every { targetableBy } returns setOf(com.manikoske.guild.action.Target.Type.SingleAlly)
            }
        }

        val restrictSingleEnemyStatus = mockk<Status> {
            every { targetabilityAlteringEffect } returns mockk {
                every { targetableBy } returns setOf(com.manikoske.guild.action.Target.Type.SingleEnemy)
            }
        }

        val stateWithNoEffect = Fixture.characterState().copy(
            character = character,
            statuses = listOf(noEffectStatus)
        )

        val stateRestrictingSingleAlly = Fixture.characterState().copy(
            character = character,
            statuses = listOf(restrictSingleAllyStatus)
        )

        val stateRestrictingSingleEnemyAndAlly = Fixture.characterState().copy(
            character = character,
            statuses = listOf(restrictSingleAllyStatus, restrictSingleEnemyStatus)
        )

        // Act & Assert

        // Without alteration effects
        assertThat(stateWithNoEffect.targetableBy(com.manikoske.guild.action.Target.Type.SingleAlly)).isTrue()
        assertThat(stateWithNoEffect.targetableBy(com.manikoske.guild.action.Target.Type.NodeEnemy)).isTrue()

        // With restriction (Single Ally only)
        assertThat(stateRestrictingSingleAlly.targetableBy(com.manikoske.guild.action.Target.Type.SingleAlly)).isTrue()
        assertThat(stateRestrictingSingleAlly.targetableBy(com.manikoske.guild.action.Target.Type.SingleEnemy)).isFalse()

        // With multiple restrictions (intersection of Single Ally and Single Enemy)
        assertThat(stateRestrictingSingleEnemyAndAlly.targetableBy(com.manikoske.guild.action.Target.Type.SingleAlly)).isFalse()
        assertThat(stateRestrictingSingleEnemyAndAlly.targetableBy(com.manikoske.guild.action.Target.Type.SingleEnemy)).isFalse()
    }

    @Test
    fun `test addStatus scenarios`() {
        // Arrange
        val character = mockk<com.manikoske.guild.character.Character>()

        val stun = Status.StatusFactory.stun(2)
        val haste = Status.StatusFactory.haste(3)

        val hastedCharacterState = Fixture.characterState().copy(
            character = character,
            statuses = listOf(haste)
        )

        // Test Adding New Status
        val addStatusResult = hastedCharacterState.addStatus(stun)
        assertThat(addStatusResult).isInstanceOf(CharacterState.Result.AddStatusResult.Added::class.java)
        (addStatusResult as CharacterState.Result.AddStatusResult.Added).also {
            assertThat(it.addedStatus).isEqualTo(stun)
            assertThat(it.updatedTarget.statuses).containsExactlyInAnyOrder(stun, haste)
        }

        // Test Replacing Existing Status
        val stunnedCharacterState = Fixture.characterState().copy(
            character = character,
            statuses = listOf(stun, haste)
        )
        val freshStun = stun.copy(duration = Status.Duration.RoundLimited(4))
        val replaceStatusResult = stunnedCharacterState.addStatus(freshStun)
        assertThat(replaceStatusResult).isInstanceOf(CharacterState.Result.AddStatusResult.Replaced::class.java)
        (replaceStatusResult as CharacterState.Result.AddStatusResult.Replaced).also {
            assertThat(it.addedStatus).isEqualTo(freshStun)
            assertThat(it.replacedStatus).isEqualTo(stun)
            assertThat(it.updatedTarget.statuses).containsExactlyInAnyOrder(freshStun, haste)
        }
    }

    @Test
    fun `test removeStatuses scenarios`() {
        // Arrange
        val character = mockk<com.manikoske.guild.character.Character>()
        val stun = Status.StatusFactory.stun(2)
        val slow = Status.StatusFactory.slow(3)
        val haste = Status.StatusFactory.haste(1)

        val characterState = Fixture.characterState().copy(
            character = character,
            statuses = listOf(stun, slow, haste)
        )

        // Test Removing Existing Status
        val removeStunResult = characterState.removeStatuses(Status.Name.Stunned)
        assertThat(removeStunResult).isInstanceOf(CharacterState.Result.RemoveStatusesResult.Removed::class.java)
        (removeStunResult as CharacterState.Result.RemoveStatusesResult.Removed).also {
            assertThat(it.removedStatuses).containsExactly(stun)
            assertThat(it.updatedTarget.statuses).doesNotContain(stun)
        }

        // Test Removing Non-Existent Status
        val removeNonExistentResult = characterState.removeStatuses(Status.Name.Invisible)
        assertThat(removeNonExistentResult).isInstanceOf(CharacterState.Result.RemoveStatusesResult.NothingRemoved::class.java)
        (removeNonExistentResult as CharacterState.Result.RemoveStatusesResult.NothingRemoved).also {
            assertThat(it.updatedTarget.statuses).containsExactlyInAnyOrder(stun, slow, haste)
        }

        // Test Removing Multiple Instances of the Same Status
        val doubleStatusState = Fixture.characterState().copy(
            character = character,
            statuses = listOf(stun, stun, slow)
        )
        val removeDoubleStatusResult = doubleStatusState.removeStatuses(Status.Name.Stunned)
        assertThat(removeDoubleStatusResult).isInstanceOf(CharacterState.Result.RemoveStatusesResult.Removed::class.java)
        (removeDoubleStatusResult as CharacterState.Result.RemoveStatusesResult.Removed).also {
            assertThat(it.removedStatuses).containsExactlyInAnyOrder(stun, stun)
            assertThat(it.updatedTarget.statuses).containsExactly(slow)
        }
    }

    @Test
    fun `test allExecutableActions scenarios`() {
        // Arrange
        val character = mockk<com.manikoske.guild.character.Character> {
            every { maxResources() } returns 50
            every { availableActions } returns listOf(Action.Actions.hideInShadows)
        }

        val restrictingStatuses = listOf(Status.StatusFactory.disarm(1), Status.StatusFactory.silence(1))
        val forcingStatus = Status.StatusFactory.prone()
        val noActionStatus = Status.StatusFactory.down()

        // Test: No Action
        val noActionState = Fixture.characterState().copy(statuses = listOf(noActionStatus))
        assertThat(noActionState.allExecutableActions()).containsExactly(Action.Actions.noAction)

        // Test: Forced Actions
        val forcedActionState = Fixture.characterState().copy(statuses = listOf(forcingStatus))
        assertThat(forcedActionState.allExecutableActions()).containsExactlyInAnyOrder(Action.Actions.standUp, Action.Actions.crawl)

        // Test: Restricted Actions
        val restrictedActionState = Fixture.characterState().copy(
            character = character,
            resourcesSpent = 40,
            statuses = restrictingStatuses
        )
        assertThat(restrictedActionState.allExecutableActions())
            .containsExactlyInAnyOrder(Action.Actions.hideInShadows, Action.Actions.disengage, Action.Actions.dash)

        // Test: Eligible Actions
        val eligibleActionState = Fixture.characterState().copy(character = character, resourcesSpent = 20, statuses = emptyList())
        assertThat(eligibleActionState.allExecutableActions()).containsExactlyInAnyOrderElementsOf(
            Action.Actions.basicActions + character.availableActions
        )
    }
    @Test
    fun `test actualMovement scenarios`() {
        // Arrange

        val hastedCharacterState = Fixture.characterState().copy(
            statuses = listOf(Status.StatusFactory.haste(1))
        )

        val hastedAndSlowedCharacterState = Fixture.characterState().copy(
            statuses = listOf(Status.StatusFactory.haste(1), Status.StatusFactory.slow(1))
        )

        val hastedAndHeldCharacterState = Fixture.characterState().copy(
            statuses = listOf(Status.StatusFactory.haste(1), Status.StatusFactory.held(1))
        )

        val movement = Movement(Movement.Type.Special, 10)

        // Act & Assert

        assertThat(hastedCharacterState.actualMovement(movement))
            .isEqualTo(movement.copy(amount = movement.amount + 1))

        assertThat(hastedAndSlowedCharacterState.actualMovement(movement))
            .isEqualTo(movement)

        assertThat(hastedAndHeldCharacterState.actualMovement(movement))
            .isEqualTo(movement.copy(amount = 0))
    }
}