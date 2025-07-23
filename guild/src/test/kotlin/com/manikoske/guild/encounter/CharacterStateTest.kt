package com.manikoske.guild.encounter

import com.manikoske.guild.action.Effect
import com.manikoske.guild.action.Movement
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test


class CharacterStateTest {

    @Test
    fun canMoveBy() {

        val normalMovement = Movement(type = Movement.Type.Normal, amount = 2)
        val specialMovement = Movement(type = Movement.Type.Special, amount = 3)

        val entangled = Randomizer.randomBuilder().giveMeOne<Effect.MovementRestrictingEffect.Entangled>()
        val slow = Randomizer.randomBuilder().giveMeOne<Effect.MovementAlteringEffect.Slow>()
        val haste = Randomizer.randomBuilder().giveMeOne<Effect.MovementAlteringEffect.Haste>()

        val korgan = Randomizer.characterState("Korgan").copy(effects = CharacterState.noEffects())

        // no statuses, no alterations
        assertThat(korgan.canMoveBy(normalMovement, normalMovement.amount)).isTrue()
        assertThat(korgan.canMoveBy(specialMovement, specialMovement.amount)).isTrue()

        // movement prohibiting statuses only are present
        assertThat(korgan.addEffect(entangled).updatedTarget.canMoveBy(normalMovement, 1)).isFalse()

        assertThat(korgan.addEffect(entangled).updatedTarget.canMoveBy(specialMovement, specialMovement.amount)).isTrue()

        // movement altering statuses only are present

        assertThat(korgan.addEffect(slow).updatedTarget.canMoveBy(normalMovement, 2)).isFalse()
        assertThat(korgan.addEffect(slow).updatedTarget.canMoveBy(specialMovement, 3)).isFalse()

        assertThat(korgan.addEffect(haste).updatedTarget.canMoveBy(normalMovement, 3)).isTrue()
        assertThat(korgan.addEffect(haste).updatedTarget.canMoveBy(specialMovement, 4)).isTrue()

        // both movement statuses are present

        assertThat(korgan.addEffect(haste).updatedTarget.addEffect(entangled).updatedTarget.canMoveBy(normalMovement, 1)).isFalse()
        assertThat(korgan.addEffect(slow).updatedTarget.addEffect(entangled).updatedTarget.canMoveBy(normalMovement, 1)).isFalse()
    }

    @Test
    fun testRollInitiative() {

        val irenicus = Randomizer.characterState("Jon")
        val ellesime = Randomizer.characterState("Ellesime")

        irenicus.attackBy(
            attacker = ellesime,
            attackRollModifier = 1,
            damageRollMultiplier = 2,
            effectsOnHit = listOf(Effect.MovementRestrictingEffect.Entangled(roundsLeft = 2))
        )

    }

}