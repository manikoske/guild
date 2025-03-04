package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Effect
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.random.Random


class CharacterStateTest {

    @Test
    fun canMoveBy() {

        val normalMovement = Movement(type = Movement.Type.Normal, amount = 2)
        val specialMovement = Movement(type = Movement.Type.Special, amount = 3)

        val entangled = Randomizer.randomBuilder().giveMeOne<Effect.MovementRestrictingEffect.Entangled>()
        val slow = Randomizer.randomBuilder().giveMeOne<Effect.MovementAlteringEffect.Slow>()
        val haste = Randomizer.randomBuilder().giveMeOne<Effect.MovementAlteringEffect.Haste>()

        val korgan = Randomizer.characterState("Korgan").copy(effects = CharacterState.CharacterStates.noEffects())

        // no statuses, no alterations
        assertThat(korgan.canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement)
        assertThat(korgan.canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement)

        // movement prohibiting statuses only are present
        assertThat(korgan.addEffect(entangled).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 0))

        assertThat(korgan.addEffect(entangled).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement)

        // movement altering statuses only are present

        assertThat(korgan.addEffect(slow).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 1))

        assertThat(korgan.addEffect(slow).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement.copy(amount = 2))

        assertThat(korgan.addEffect(haste).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 3))

        assertThat(korgan.addEffect(haste).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement.copy(amount = 4))


        // both movement statuses only are present

        assertThat(korgan.addEffect(haste).addEffect(entangled).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 0))

        assertThat(korgan.addEffect(slow).addEffect(entangled).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 0))

    }

    @Test
    fun allExecutableActions() {
        val araken = Randomizer.characterState("Araken").copy(effects = CharacterState.CharacterStates.noEffects())

        assertThat(araken.allExecutableActions())
            .usingRecursiveComparison()
            .isEqualTo(Action.Actions.basicActions)

        assertThat(araken.addEffect(Randomizer.randomBuilder().giveMeOne<Effect.ActionForcingEffect.Stun>()).allExecutableActions())
            .usingRecursiveComparison()
            .isEqualTo(listOf(Action.ForcedAction.NoAction))


    }
}