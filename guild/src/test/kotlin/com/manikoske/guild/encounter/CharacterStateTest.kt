package com.manikoske.guild.encounter

import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Status
import com.manikoske.guild.encounter.TestingCommons.randomBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class CharacterStateTest {

    @Test
    fun canMoveBy() {

        val normalMovement = Movement(type = Movement.Type.Normal, amount = 2)
        val specialMovement = Movement(type = Movement.Type.Special, amount = 3)

        val entangled = randomBuilder.giveMeOne<Status.Entangled>()
        val prone = randomBuilder.giveMeOne<Status.Prone>()
        val slow = randomBuilder.giveMeOne<Status.Slow>()
        val haste = randomBuilder.giveMeOne<Status.Haste>()

        val characterState = randomBuilder.giveMeOne<CharacterState>()

        // no statuses no alterations
        assertThat(characterState.copy(statuses = listOf()).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement)

        assertThat(characterState.copy(statuses = listOf()).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement)


        // movement prohibiting statuses only are present
        assertThat(characterState.copy(statuses = listOf(entangled)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 0))

        assertThat(characterState.copy(statuses = listOf(entangled)).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement)

        assertThat(characterState.copy(statuses = listOf(prone)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 0))

        assertThat(characterState.copy(statuses = listOf(prone)).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement.copy(amount = 0))

        assertThat(characterState.copy(statuses = listOf(prone, entangled)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 0))

        assertThat(characterState.copy(statuses = listOf(prone, entangled)).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement.copy(amount = 0))

        // movement altering statuses only are present

        assertThat(characterState.copy(statuses = listOf(slow)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 1))

        assertThat(characterState.copy(statuses = listOf(slow)).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement.copy(amount = 2))

        assertThat(characterState.copy(statuses = listOf(haste)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 3))

        assertThat(characterState.copy(statuses = listOf(haste)).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement.copy(amount = 4))

        assertThat(characterState.copy(statuses = listOf(slow, haste)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement)

        assertThat(characterState.copy(statuses = listOf(slow, haste)).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement)

        // both movement statuses only are present

        assertThat(characterState.copy(statuses = listOf(haste, entangled)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 0))

        assertThat(characterState.copy(statuses = listOf(haste, prone)).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement.copy(amount = 0))

        assertThat(characterState.copy(statuses = listOf(slow, entangled)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 0))

        assertThat(characterState.copy(statuses = listOf(slow, prone)).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement.copy(amount = 0))

    }
}