package com.manikoske.guild.encounter

import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Effect
import com.manikoske.guild.encounter.TestingCommons.randomBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class CharacterStateTest {

    @Test
    fun canMoveBy() {

        val normalMovement = Movement(type = Movement.Type.Normal, amount = 2)
        val specialMovement = Movement(type = Movement.Type.Special, amount = 3)

        val entangled = randomBuilder.giveMeOne<Effect.MovementRestrictingEffect.Entangled>()
        val slow = randomBuilder.giveMeOne<Effect.MovementAlteringEffect.Slow>()
        val haste = randomBuilder.giveMeOne<Effect.MovementAlteringEffect.Haste>()

        val characterState = randomBuilder.giveMeOne<CharacterState>()

        // no statuses, no alterations
        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects()).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement)

        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects()).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement)


        // movement prohibiting statuses only are present
        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects().addEffect(entangled)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 0))

        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects().addEffect(entangled)).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement)

        // movement altering statuses only are present

        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects().addEffect(slow)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 1))

        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects().addEffect(slow)).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement.copy(amount = 2))

        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects().addEffect(haste)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 3))

        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects().addEffect(haste)).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement.copy(amount = 4))


        // both movement statuses only are present

        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects().addEffect(haste).addEffect(entangled)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 0))

        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects().addEffect(slow).addEffect(entangled)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 0))


    }
}